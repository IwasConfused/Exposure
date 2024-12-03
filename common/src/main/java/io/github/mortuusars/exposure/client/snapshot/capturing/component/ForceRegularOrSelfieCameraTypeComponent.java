package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class ForceRegularOrSelfieCameraTypeComponent implements CaptureComponent {
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
