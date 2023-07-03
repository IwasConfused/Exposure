package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.component.ShutterSpeed;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class CaptureProperties {
    public final String id;
    public final int size;
    public final float cropFactor;
    public final ShutterSpeed shutterSpeed;
    public final List<IExposureModifier> modifiers;

    public CaptureProperties(String id, int size, float cropFactor, ShutterSpeed shutterSpeed, List<IExposureModifier> modifiers) {
        this.id = id;
        this.size = size;
        this.cropFactor = cropFactor;
        this.shutterSpeed = shutterSpeed;
        this.modifiers = modifiers;
    }
}
