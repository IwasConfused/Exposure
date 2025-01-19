package io.github.mortuusars.exposure.client.capture.template;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.exposure.client.image.modifier.Modifier;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.cycles.task.EmptyTask;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionInfo;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;

public class PathCaptureTemplate implements CaptureTemplate {
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

        if (data.projection().isEmpty()) {
            LOGGER.error("Cannot load: projecting info is missing. {}", data);
            return new EmptyTask<>();
        }

        ProjectionInfo projectionInfo = data.projection().get();
        String path = projectionInfo.path();
        int frameSize = data.frameSize().orElse(Config.Server.DEFAULT_FRAME_SIZE.getAsInt());
        float brightnessStops = data.shutterSpeed().orElse(ShutterSpeed.DEFAULT).getStopsDifference(ShutterSpeed.DEFAULT);
        Holder<ColorPalette> palette = data.colorPalette().orElse(ColorPalettes.getDefault(Minecrft.registryAccess()));

        return Capture.of(Capture.path(path))
                .logErrorAndGetResult(LOGGER)
                .thenAsync(Modifier.chain(
                        Modifier.Crop.SQUARE_CENTER,
                        Modifier.Resize.to(frameSize),
                        Modifier.brightness(brightnessStops),
                        chooseColorProcessor(data)))
                .thenAsync(Palettizer.fromProjectionMode(projectionInfo.mode()).palettizeAndClose(palette.value()))
                .thenAsync(convertToExposureData(palette, createExposureTag(photographer.getExecutingPlayer(), data, true)))
                .acceptAsync(image -> ExposureUploader.upload(data.exposureId(), image))
                .onError(err -> LOGGER.error(err.technical().getString()));
    }
}
