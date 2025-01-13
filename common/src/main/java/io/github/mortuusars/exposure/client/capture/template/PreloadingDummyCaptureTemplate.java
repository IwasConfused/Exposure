package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureActions;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.processor.Process;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.data.ColorPalettes;
import net.minecraft.client.CameraType;
import org.jetbrains.annotations.Nullable;

public class PreloadingDummyCaptureTemplate implements CaptureTemplate {
    @Override
    public Task<?> createTask(@Nullable CaptureProperties data) {
        float brightnessStops = 2;

        ColorPalette palette = ColorPalettes.get(Minecrft.registryAccess(), ColorPalettes.DEFAULT).value();

        return Capture.of(Capture.screenshot(),
                        CaptureActions.hideGui(),
                        CaptureActions.forceCamera(CameraType.FIRST_PERSON),
                        CaptureActions.setFov(50),
                        CaptureActions.forceRegularOrSelfieCamera(),
                        CaptureActions.disablePostEffect(),
                        CaptureActions.modifyGamma(brightnessStops))
                .handleErrorAndGetResult()
                .thenAsync(Process.with(
                        Processor.Crop.SQUARE_CENTER,
                        Processor.Crop.factor(1),
                        Processor.Resize.to(16),
                        Processor.brightness(brightnessStops),
                        Processor.blackAndWhite(1)))
                .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette))
                .thenAsync(img -> ExposureData.EMPTY);
    }
}
