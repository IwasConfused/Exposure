package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.action.CaptureActions;
import io.github.mortuusars.exposure.client.capture.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.image.processor.Process;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.capture.saving.PalettedExposureUploader;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.core.CaptureProperties;
import io.github.mortuusars.exposure.core.FileProjectingInfo;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.core.cycles.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CameraCaptureTemplate implements CaptureTemplate {
    @Override
    public Task<?> createTask(LocalPlayer localPlayer, String id, CaptureProperties data) {
        Entity cameraHolder = data.photographer().asEntity();

        float brightnessStops = data.shutterSpeed().getStopsDifference(ShutterSpeed.DEFAULT);

        Task<PalettedExposure> captureTask = Capture.of(Capture.screenshot(),
                        CaptureAction.optional(!data.photographer().getExecutingPlayer().equals(cameraHolder),
                                () -> CaptureActions.setCameraEntity(cameraHolder)),
                        CaptureActions.hideGui(),
                        CaptureActions.forceRegularOrSelfieCamera(),
                        CaptureActions.disablePostEffect(),
                        CaptureActions.modifyGamma(brightnessStops),
                        CaptureAction.optional(data.flashHasFired(), () -> CaptureActions.flash(cameraHolder)))
                .handleErrorAndGetResult(printCasualErrorInChat())
                .then(Process.with(
                        Processor.Crop.SQUARE_CENTER,
                        Processor.Crop.factor(data.cropFactor()),
                        Processor.Resize.to(data.frameSize()),
                        Processor.brightness(brightnessStops),
                        chooseColorProcessor(data)))
                .thenAsync(image -> ImagePalettizer.palettizeAndClose(image, ColorPalette.MAP_COLORS, true))
                .then(image -> new PalettedExposure(image.getWidth(), image.getHeight(),
                        image.getPixels(), image.getPalette(), createExposureTag(data, false)));

        if (data.fileProjectingInfo().isPresent()) {
            FileProjectingInfo fileLoadingData = data.fileProjectingInfo().get();
            String filepath = fileLoadingData.getFilepath();
            boolean dither = fileLoadingData.shouldDither();

            captureTask = captureTask.overridenBy(Capture.of(Capture.file(filepath),
                            CaptureActions.interplanarProjection(data.photographer(), data.cameraID()))
                    .handleErrorAndGetResult(printCasualErrorInChat())
                    .then(Process.with(
                            Processor.Crop.SQUARE_CENTER,
                            Processor.Resize.to(data.frameSize()),
                            Processor.brightness(brightnessStops),
                            chooseColorProcessor(data)))
                    .thenAsync(image -> ImagePalettizer.palettizeAndClose(image, ColorPalette.MAP_COLORS, dither))
                    .then(image -> new PalettedExposure(image.getWidth(), image.getHeight(),
                            image.getPixels(), image.getPalette(), createExposureTag(data, true))));
        }

        return captureTask
                .accept(image -> PalettedExposureUploader.upload(data.id(), image))
                .onError(printCasualErrorInChat());
    }

    private static PalettedExposure.Tag createExposureTag(CaptureProperties data, boolean isFromFile) {
        return new PalettedExposure.Tag(data.filmType(), data.photographer().getExecutingPlayer().getScoreboardName(),
                UnixTimestamp.Seconds.now(), isFromFile, false);
    }

    protected Processor chooseColorProcessor(CaptureProperties data) {
        return data.filmType() == ExposureType.BLACK_AND_WHITE
                ? data.chromaChannel().map(Processor::singleChannelBlackAndWhite).orElse(Processor.blackAndWhite(1.15f))
                : Processor.EMPTY;
    }

    protected @NotNull Consumer<TranslatableError> printCasualErrorInChat() {
        return err -> Minecrft.execute(() ->
                Minecrft.player().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false));
    }
}
