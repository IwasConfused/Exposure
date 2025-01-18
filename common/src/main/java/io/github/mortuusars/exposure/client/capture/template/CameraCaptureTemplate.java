package io.github.mortuusars.exposure.client.capture.template;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureActions;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.processor.Process;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionMode;
import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionInfo;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.EmptyTask;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;

public class CameraCaptureTemplate implements CaptureTemplate {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public Task<?> createTask(CaptureProperties data) {
        if (data.exposureId().isEmpty()) {
            LOGGER.error("Failed to create capture task: exposure id cannot be empty. '{}'", data);
            return new EmptyTask<>();
        }

        UUID photographerUUID = data.photographerEntityId().orElse(Minecrft.player().getUUID());
        @Nullable PhotographerEntity photographer = PhotographerEntity.fromUuid(Minecrft.level(), photographerUUID).orElse(null);

        if (photographer == null) {
            LOGGER.error("Failed to create capture task: photographer cannot be obtained. '{}'", data);
            return new EmptyTask<>();
        }

        Entity cameraHolder = photographer.asEntity();
        float brightnessStops = data.shutterSpeed().orElse(ShutterSpeed.DEFAULT).getStopsDifference(ShutterSpeed.DEFAULT);
        Holder<ColorPalette> colorPaletteHolder = data.colorPalette().orElse(ColorPalettes.getDefault(Minecrft.registryAccess()));
        ColorPalette palette = colorPaletteHolder.value();
        ResourceLocation paletteId = colorPaletteHolder.unwrapKey().orElseThrow().location();

        Task<ExposureData> captureTask = Capture.of(Capture.screenshot(),
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
                        Processor.Crop.factor(data.cropFactor().orElse(1F)),
                        Processor.Resize.to(data.frameSize().orElse(Config.Server.DEFAULT_FRAME_SIZE.getAsInt())),
                        Processor.brightness(brightnessStops),
                        chooseColorProcessor(data)))
                .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette))
                .thenAsync(convertToExposureData(paletteId, createExposureTag(photographer.getExecutingPlayer(), data, false)));

        if (data.projection().isPresent()) {
            ProjectionInfo projectionInfo = data.projection().get();
            String path = projectionInfo.path();
            Palettizer palettizer = projectionInfo.mode() == ProjectionMode.DITHERED ? Palettizer.DITHERED : Palettizer.NEAREST;

            captureTask = captureTask.overridenBy(Capture.of(Capture.path(path),
                            CaptureActions.optional(data.cameraId(),
                                    id -> CaptureActions.interplanarProjection(photographer, id)))
                    .handleErrorAndGetResult(err -> LOGGER.error(err.technical().getString()))
                    .thenAsync(Process.with(
                            Processor.Crop.SQUARE_CENTER,
                            Processor.Resize.to(data.frameSize().orElse(Config.Server.DEFAULT_FRAME_SIZE.getAsInt())),
                            Processor.brightness(brightnessStops),
                            chooseColorProcessor(data)))
                    .thenAsync(Palettizer.palettizeAndClose(palettizer, palette))
                    .thenAsync(convertToExposureData(paletteId, createExposureTag(photographer.getExecutingPlayer(), data, true))));
        }

        return captureTask
                .acceptAsync(image -> ExposureUploader.upload(data.exposureId(), image))
                .onError(printCasualErrorInChat());
    }
}
