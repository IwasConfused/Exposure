package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureActions;
import io.github.mortuusars.exposure.client.capture.palettizer.ImagePalettizer;
import io.github.mortuusars.exposure.client.image.processor.Process;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.capture.saving.PalettedExposureUploader;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.core.CaptureProperties;
import io.github.mortuusars.exposure.core.FileLoadingInfo;
import io.github.mortuusars.exposure.core.cycles.task.EmptyTask;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.core.cycles.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CameraCaptureTemplate implements CaptureTemplate {
    @Override
    public Task<?> createTask(CaptureProperties data) {
        @Nullable PhotographerEntity photographer = PhotographerEntity.fromUUID(Minecrft.level(), data.photographerEntityID())
                .orElse(null);

        if (photographer == null) {
            Exposure.LOGGER.error("Failed to create capture task: photographer cannot be obtained. '{}'", data);
            return new EmptyTask<>();
        }

        Entity cameraHolder = photographer.asEntity();

        float brightnessStops = data.shutterSpeed().getStopsDifference(ShutterSpeed.DEFAULT);

        Task<PalettedExposure> captureTask = Capture.of(Capture.screenshot(),
                        CaptureActions.optional(!photographer.getExecutingPlayer().equals(cameraHolder),
                                () -> CaptureActions.setCameraEntity(cameraHolder)),
                        CaptureActions.hideGui(),
                        CaptureActions.forceRegularOrSelfieCamera(),
                        CaptureActions.disablePostEffect(),
                        CaptureActions.modifyGamma(brightnessStops),
                        CaptureActions.optional(data.flash(), () -> CaptureActions.flash(cameraHolder)))
                .handleErrorAndGetResult(printCasualErrorInChat())
                .thenAsync(Process.with(
                        Processor.Crop.SQUARE_CENTER,
                        Processor.Crop.factor(data.cropFactor()),
                        Processor.Resize.to(data.frameSize()),
                        Processor.brightness(brightnessStops),
                        chooseColorProcessor(data)))
                .thenAsync(image -> ImagePalettizer.palettizeAndClose(image, data.colorPalette(), true))
                .thenAsync(image -> new PalettedExposure(image.getWidth(), image.getHeight(),
                        image.getPixels(), image.getPalette(), createExposureTag(data, photographer, false)));

        if (data.fileLoadingInfo().isPresent()) {
            FileLoadingInfo fileLoadingData = data.fileLoadingInfo().get();
            String filepath = fileLoadingData.getFilepath();
            boolean dither = fileLoadingData.shouldDither();

            captureTask = captureTask.overridenBy(Capture.of(Capture.file(filepath),
                            CaptureActions.optional(data.cameraID(),
                                    id -> CaptureActions.interplanarProjection(photographer, id)))
                    .handleErrorAndGetResult(printCasualErrorInChat())
                    .thenAsync(Process.with(
                            Processor.Crop.SQUARE_CENTER,
                            Processor.Resize.to(data.frameSize()),
                            Processor.brightness(brightnessStops),
                            chooseColorProcessor(data)))
                    .thenAsync(image -> ImagePalettizer.palettizeAndClose(image, data.colorPalette(), dither))
                    .thenAsync(image -> new PalettedExposure(image.getWidth(), image.getHeight(),
                            image.getPixels(), image.getPalette(), createExposureTag(data,  photographer, true))));
        }

        if (data.exposureID().isEmpty()) {
            return captureTask;
        }

        return captureTask
                .acceptAsync(image -> PalettedExposureUploader.upload(data.exposureID(), image))
                .onError(printCasualErrorInChat());
    }

    protected Processor chooseColorProcessor(CaptureProperties data) {
        return data.filmType() == ExposureType.BLACK_AND_WHITE
                ? data.chromaChannel().map(Processor::singleChannelBlackAndWhite).orElse(Processor.blackAndWhite(1.15f))
                : Processor.EMPTY;
    }

    protected PalettedExposure.Tag createExposureTag(CaptureProperties data, PhotographerEntity photographer, boolean isFromFile) {
        return new PalettedExposure.Tag(data.filmType(), photographer.getExecutingPlayer().getScoreboardName(),
                UnixTimestamp.Seconds.now(), isFromFile, false);
    }

    protected @NotNull Consumer<TranslatableError> printCasualErrorInChat() {
        return err -> Minecrft.execute(() ->
                Minecrft.player().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false));
    }
}
