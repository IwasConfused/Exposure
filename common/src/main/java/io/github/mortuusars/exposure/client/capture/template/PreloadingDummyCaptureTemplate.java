package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.modifier.ImageModifier;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.data.ColorPalettes;
import net.minecraft.client.CameraType;
import org.jetbrains.annotations.Nullable;

public class PreloadingDummyCaptureTemplate implements CaptureTemplate {
    @Override
    public Task<?> createTask(@Nullable CaptureProperties data) {
        ColorPalette palette = ColorPalettes.get(Minecrft.registryAccess(), ColorPalettes.DEFAULT).value();

        return Capture.of(Capture.screenshot(),
                        CaptureAction.hideGui(),
                        CaptureAction.forceCamera(CameraType.FIRST_PERSON),
                        CaptureAction.setFov(50),
                        CaptureAction.forceRegularOrSelfieCamera(),
                        CaptureAction.disablePostEffect(),
                        CaptureAction.modifyGamma(new ShutterSpeed("1/15")))
                .handleErrorAndGetResult()
                .thenAsync(ImageModifier.chain(
                        ImageModifier.Crop.SQUARE_CENTER,
                        ImageModifier.Crop.factor(1),
                        ImageModifier.Resize.to(16),
                        ImageModifier.brightness(new ShutterSpeed("1/15")),
                        ImageModifier.BLACK_AND_WHITE))
                .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette))
                .thenAsync(img -> ExposureData.EMPTY);
    }
}
