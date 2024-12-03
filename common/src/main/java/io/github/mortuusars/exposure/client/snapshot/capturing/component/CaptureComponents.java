package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import java.util.function.Supplier;

public interface CaptureComponents {
    static ForceFirstPersonComponent forceFirstPerson() {
        return new ForceFirstPersonComponent();
    }

    static HideGuiComponent hideGui() {
        return new HideGuiComponent();
    }

    static DisablePostEffectComponent disablePostEffect() {
        return new DisablePostEffectComponent();
    }

    static CaptureComponent optional(boolean predicate, Supplier<CaptureComponent> componentSupplier) {
        return predicate ? componentSupplier.get() : CaptureComponent.EMPTY;
    }

    static CaptureComponent optional(boolean predicate, CaptureComponent component) {
        return predicate ? component : CaptureComponent.EMPTY;
    }
}
