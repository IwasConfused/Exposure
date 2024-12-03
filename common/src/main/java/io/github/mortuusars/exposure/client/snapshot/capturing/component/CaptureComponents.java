package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import net.minecraft.client.CameraType;
import net.minecraft.world.entity.Entity;

import java.util.function.Supplier;

public interface CaptureComponents {
    static ForceCameraTypeComponent forceCamera(CameraType cameraType) {
        return new ForceCameraTypeComponent(cameraType);
    }

    static ForceRegularOrSelfieCameraTypeComponent forceRegularOrSelfieCamera() {
        return new ForceRegularOrSelfieCameraTypeComponent();
    }

    static HideGuiComponent hideGui() {
        return new HideGuiComponent();
    }

    static DisablePostEffectComponent disablePostEffect() {
        return new DisablePostEffectComponent();
    }

    static ModifyGammaComponent modifyGamma(int brightnessStops) {
        return new ModifyGammaComponent(brightnessStops);
    }

    static FlashComponent flash(Entity photographer) {
        return new FlashComponent(photographer);
    }

    static CaptureComponent optional(boolean predicate, Supplier<CaptureComponent> componentSupplier) {
        return predicate ? componentSupplier.get() : CaptureComponent.EMPTY;
    }

    static CaptureComponent optional(boolean predicate, CaptureComponent component) {
        return predicate ? component : CaptureComponent.EMPTY;
    }
}
