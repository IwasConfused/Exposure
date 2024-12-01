package io.github.mortuusars.exposure.client.snapshot.capturing.component;

public interface CaptureComponents {
    static HideGuiCaptureComponent hideGui() {
        return new HideGuiCaptureComponent();
    }

    static ForceFirstPersonCaptureComponent forceFirstPerson() {
        return new ForceFirstPersonCaptureComponent();
    }
}
