package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class ForceCameraTypeComponent implements CaptureComponent {
    private final CameraType forcedCameraType;
    private CameraType cameraTypeBeforeCapture = CameraType.FIRST_PERSON;

    public ForceCameraTypeComponent(CameraType forcedCameraType) {
        this.forcedCameraType = forcedCameraType;
    }

    @Override
    public void beforeCapture() {
        cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();
        Minecraft.getInstance().options.setCameraType(forcedCameraType);
    }

    @Override
    public void afterCapture() {
        Minecraft.getInstance().options.setCameraType(cameraTypeBeforeCapture);
    }
}
