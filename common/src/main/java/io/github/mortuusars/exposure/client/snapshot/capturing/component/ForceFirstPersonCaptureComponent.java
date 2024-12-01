package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class ForceFirstPersonCaptureComponent implements CaptureComponent {
    @Nullable
    private CameraType cameraTypeBeforeCapture;

    @Override
    public void beforeCapture() {
        cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
    }

    @Override
    public void afterCapture() {
        CameraType cameraType = cameraTypeBeforeCapture != null ? cameraTypeBeforeCapture : CameraType.FIRST_PERSON;
        Minecraft.getInstance().options.setCameraType(cameraType);
    }
}
