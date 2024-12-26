package io.github.mortuusars.exposure.client.snapshot.template;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.image.PalettizedImage;
import io.github.mortuusars.exposure.client.snapshot.capture.Capture;
import io.github.mortuusars.exposure.client.snapshot.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.snapshot.capture.action.CaptureActions;
import io.github.mortuusars.exposure.client.snapshot.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.snapshot.processing.Process;
import io.github.mortuusars.exposure.client.snapshot.processing.Processor;
import io.github.mortuusars.exposure.client.snapshot.saving.ImageUploader;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.core.frame.CaptureData;
import io.github.mortuusars.exposure.core.frame.FileProjectingInfo;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CameraCaptureTemplate implements CaptureTemplate {
    @Override
    public Task<?> createTask(LocalPlayer localPlayer, ExposureIdentifier identifier, CaptureData data) {
        Preconditions.checkNotNull(Minecraft.getInstance().level, "Minecraft.getInstance().level");

        Entity cameraHolder = data.photographer().asEntity();

        int frameSize = data.frameSize();
        float brightnessStops = data.shutterSpeed().getStopsDifference(ShutterSpeed.DEFAULT);

        Task<PalettizedImage> captureTask = Capture.of(Capture.screenshot(),
                        CaptureAction.optional(!data.photographer().getExecutingPlayer().equals(cameraHolder),
                                () -> CaptureActions.setCameraEntity(cameraHolder)),
                        CaptureActions.hideGui(),
                        CaptureActions.forceRegularOrSelfieCamera(),
                        CaptureActions.disablePostEffect(),
                        CaptureActions.modifyGamma(brightnessStops),
                        CaptureAction.optional(data.flashHasFired(), () -> CaptureActions.flash(cameraHolder)))
                .handleErrorAndGetResult(printCasualErrorInChat())
                .thenAsync(Process.with(
                        Processor.Crop.SQUARE,
                        Processor.Crop.factor(data.cropFactor()),
                        Processor.Resize.to(frameSize),
                        Processor.brightness(brightnessStops),
                        chooseColorProcessor(data)))
                .thenAsync(image -> {
                    PalettizedImage palettizedImage = ImagePalettizer.DITHERED_MAP_COLORS.palettize(image, ColorPalette.MAP_COLORS);
                    image.close();
                    return palettizedImage;
                });

        if (data.fileProjectingInfo().isPresent()) {
            FileProjectingInfo fileLoadingData = data.fileProjectingInfo().get();
            String filepath = fileLoadingData.getFilepath();
            boolean dither = fileLoadingData.shouldDither();

            captureTask = captureTask.overridenBy(Capture.of(Capture.file(filepath),
                            CaptureActions.interplanarProjection(data.photographer(), data.cameraID()))
                    .handleErrorAndGetResult(printCasualErrorInChat())
                    .thenAsync(Process.with(
                            Processor.Crop.SQUARE,
                            Processor.Resize.to(frameSize),
                            Processor.brightness(brightnessStops),
                            chooseColorProcessor(data)))
                    .thenAsync(image -> {
                        PalettizedImage palettizedImage = (dither
                                ? ImagePalettizer.DITHERED_MAP_COLORS
                                : ImagePalettizer.NEAREST_MAP_COLORS).palettize(image, ColorPalette.MAP_COLORS);
                        image.close();
                        return palettizedImage;
                    }));
        }

        return captureTask
                .acceptAsync(new ImageUploader(identifier)::upload)
                .onError(printCasualErrorInChat());
    }

    protected Processor chooseColorProcessor(CaptureData data) {
        if (data.filmType() == ExposureType.COLOR) {
            return Processor.EMPTY;
        }

        return data.chromaChannel()
                .map(Processor::singleChannelBlackAndWhite).
                orElse(Processor.blackAndWhite());
    }

    protected @NotNull Consumer<TranslatableError> printCasualErrorInChat() {
        return err -> Minecraft.getInstance().execute(() -> {
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false);
        });
    }
}
