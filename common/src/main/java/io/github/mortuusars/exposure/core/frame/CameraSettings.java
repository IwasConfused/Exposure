package io.github.mortuusars.exposure.core.frame;

import io.github.mortuusars.exposure.core.camera.ShutterSpeed;

public record CameraSettings(ShutterSpeed shutterSpeed,
                             int focalLength,
                             boolean flash,
                             boolean selfie) {
}
