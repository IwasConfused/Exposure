package io.github.mortuusars.exposure.client.capture.template;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.exposure.client.image.modifier.ImageModifier;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.EmptyTask;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionInfo;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.core.Holder;
import org.slf4j.Logger;

public class PathCaptureTemplate implements CaptureTemplate {
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

        if (data.projection().isEmpty()) {
            LOGGER.error("Cannot load: projecting info is missing. {}", data);
            return new EmptyTask<>();
        }

        ProjectionInfo projectionInfo = data.projection().get();
        String path = projectionInfo.path();
        ExposureType filmType = data.filmType();
        int frameSize = data.frameSize().orElse(Config.Server.DEFAULT_FRAME_SIZE.getAsInt());
        ShutterSpeed shutterSpeed = data.shutterSpeed().orElse(ShutterSpeed.DEFAULT);
        Holder<ColorPalette> palette = data.getColorPalette(Minecrft.registryAccess());

        return Capture.of(Capture.path(path))
                .logErrorAndGetResult(LOGGER)
                .thenAsync(ImageModifier.chain(
                        ImageModifier.Crop.SQUARE_CENTER,
                        ImageModifier.Resize.to(frameSize),
                        ImageModifier.brightness(shutterSpeed),
                        ImageModifier.optional(filmType == ExposureType.BLACK_AND_WHITE,
                                data.singleChannel()
                                        .map(ImageModifier::singleChannelBlackAndWhite)
                                        .orElse(ImageModifier.BLACK_AND_WHITE))))
                .thenAsync(Palettizer.fromProjectionMode(projectionInfo.mode()).palettizeAndClose(palette.value()))
                .thenAsync(convertToExposureData(palette, createExposureTag(cameraHolder.getPlayerExecutingExposure(), data, true)))
                .acceptAsync(image -> ExposureUploader.upload(data.exposureId(), image))
                .onError(err -> LOGGER.error(err.technical().getString()));
    }
}
