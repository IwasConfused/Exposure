package io.github.mortuusars.exposure.client.capture.action;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class ForceRegularOrSelfieCameraTypeAction implements CaptureAction {
    private CameraType cameraTypeBeforeCapture = CameraType.FIRST_PERSON;

    @Override
    public void beforeCapture() {
        cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();
        if (cameraTypeBeforeCapture == CameraType.THIRD_PERSON_BACK) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    @Override
    public void afterCapture() {
        Minecraft.getInstance().options.setCameraType(cameraTypeBeforeCapture);
    }
}
