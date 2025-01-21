package io.github.mortuusars.exposure.client.capture.template;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureActions;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.modifier.Modifier;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionInfo;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.EmptyTask;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class CameraCaptureTemplate implements CaptureTemplate {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public Task<?> createTask(CaptureProperties data) {
        if (data.exposureId().isEmpty()) {
            LOGGER.error("Failed to create capture task: exposure id cannot be empty. '{}'", data);
            return new EmptyTask<>();
        }

        int entityId =  data.cameraHolderEntityId().orElse(Minecrft.player().getId());
        if (!(Minecrft.level().getEntity(entityId) instanceof CameraHolder cameraHolder)) {
            LOGGER.error("Failed to create capture task: camera holder cannot be obtained. '{}'", data);
            return new EmptyTask<>();
        }

        Entity entity = cameraHolder.asEntity();
        float brightnessStops = data.shutterSpeed().orElse(ShutterSpeed.DEFAULT).getStopsDifference(ShutterSpeed.DEFAULT);
        int frameSize = data.frameSize().orElse(Config.Server.DEFAULT_FRAME_SIZE.getAsInt());
        Holder<ColorPalette> palette = data.colorPalette().orElse(ColorPalettes.getDefault(Minecrft.registryAccess()));

        Task<ExposureData> captureTask = Capture.of(Capture.screenshot(),
                        CaptureActions.setCameraEntity(entity),
                        CaptureActions.hideGui(),
                        CaptureActions.forceRegularOrSelfieCamera(),
                        CaptureActions.disablePostEffect(),
                        CaptureActions.modifyGamma(brightnessStops),
                        CaptureActions.optional(data.flash(), () -> CaptureActions.flash(entity)))
                .handleErrorAndGetResult(printCasualErrorInChat())
                .thenAsync(Modifier.chain(
                        Modifier.Crop.SQUARE_CENTER,
                        Modifier.optional(data.cropFactor(), factor -> Modifier.Crop.factor(factor)),
                        Modifier.Resize.to(frameSize),
                        Modifier.brightness(brightnessStops),
                        chooseColorProcessor(data)))
                .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette.value()))
                .thenAsync(convertToExposureData(palette, createExposureTag(cameraHolder.getPlayerExecutingExposure(), data, false)));

        if (data.projection().isPresent()) {
            ProjectionInfo projectionInfo = data.projection().get();
            String path = projectionInfo.path();

            captureTask = captureTask.overridenBy(Capture.of(Capture.path(path),
                            CaptureActions.optional(data.cameraId(), CaptureActions::interplanarProjection))
                    .logErrorAndGetResult(LOGGER)
                    .thenAsync(Modifier.chain(
                            Modifier.Crop.SQUARE_CENTER,
                            Modifier.Resize.to(frameSize),
                            Modifier.brightness(brightnessStops),
                            chooseColorProcessor(data)))
                    .thenAsync(Palettizer.fromProjectionMode(projectionInfo.mode()).palettizeAndClose(palette.value()))
                    .thenAsync(convertToExposureData(palette, createExposureTag(cameraHolder.getPlayerExecutingExposure(), data, true))));
        }

        return captureTask
                .acceptAsync(image -> ExposureUploader.upload(data.exposureId(), image))
                .onError(printCasualErrorInChat());
    }
}
