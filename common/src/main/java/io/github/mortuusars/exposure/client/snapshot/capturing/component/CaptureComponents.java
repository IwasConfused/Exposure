package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import java.util.function.Supplier;

public interface CaptureComponents {
    static HideGuiCaptureComponent hideGui() {
        return new HideGuiCaptureComponent();
    }

    static ForceFirstPersonCaptureComponent forceFirstPerson() {
        return new ForceFirstPersonCaptureComponent();
    }

    static CaptureComponent optional(boolean predicate, Supplier<CaptureComponent> componentSupplier) {
        return predicate ? componentSupplier.get() : CaptureComponent.EMPTY;
    }

    static CaptureComponent optional(boolean predicate, CaptureComponent component) {
        return predicate ? component : CaptureComponent.EMPTY;
    }
}
