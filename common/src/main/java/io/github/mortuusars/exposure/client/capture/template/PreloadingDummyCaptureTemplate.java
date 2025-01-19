package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureActions;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.modifier.Modifier;
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
                .thenAsync(Modifier.chain(
                        Modifier.Crop.SQUARE_CENTER,
                        Modifier.Crop.factor(1),
                        Modifier.Resize.to(16),
                        Modifier.brightness(brightnessStops),
                        Modifier.BLACK_AND_WHITE))
                .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette))
                .thenAsync(img -> ExposureData.EMPTY);
    }
}
