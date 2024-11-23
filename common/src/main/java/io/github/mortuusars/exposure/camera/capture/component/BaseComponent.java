package io.github.mortuusars.exposure.camera.capture.component;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.camera.capture.Capture;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class BaseComponent implements ICaptureComponent {
    protected final boolean hideGuiOnCapture;
    protected boolean hideGuiBeforeCapture;
    protected CameraType cameraTypeBeforeCapture;
    protected boolean postEffectActiveBeforeCapture;

    public BaseComponent(boolean hideGuiOnCapture) {
        this.hideGuiOnCapture = hideGuiOnCapture;
        hideGuiBeforeCapture = false;
        cameraTypeBeforeCapture = CameraType.FIRST_PERSON;
        postEffectActiveBeforeCapture = true;
    }

    public BaseComponent() {
        this(true);
    }

    @Override
    public int getFramesDelay(Capture capture) {
        return Config.Client.CAPTURE_DELAY_FRAMES.get();
    }

//    @Override
//    public void onDelayFrame(Capture capture, int delayFramesLeft) {
//        if (delayFramesLeft == Math.max(0, getFramesDelay(capture) - 1)) { // Right before capturing
//            Minecraft mc = Minecraft.getInstance();
//            hideGuiBeforeCapture = mc.options.hideGui;
//            cameraTypeBeforeCapture = mc.options.getCameraType();
//            postEffectActiveBeforeCapture = Minecraft.getInstance().gameRenderer.effectActive;
//
//            mc.options.hideGui = hideGuiOnCapture;
//            CameraType cameraType = Minecraft.getInstance().options.getCameraType()
//                    == CameraType.THIRD_PERSON_FRONT ? CameraType.THIRD_PERSON_FRONT : CameraType.FIRST_PERSON;
//            mc.options.setCameraType(cameraType);
//
//
//            if (Config.Client.DISABLE_POST_EFFECT.get()) {
//                Minecraft.getInstance().gameRenderer.effectActive = false;
//            }
//        }
//    }

    @Override
    public void beforeCapture(Capture capture) {
        Minecraft mc = Minecraft.getInstance();
        hideGuiBeforeCapture = mc.options.hideGui;
        cameraTypeBeforeCapture = mc.options.getCameraType();
        postEffectActiveBeforeCapture = Minecraft.getInstance().gameRenderer.effectActive;

        mc.options.hideGui = hideGuiOnCapture;
        CameraType cameraType = Minecraft.getInstance().options.getCameraType()
                == CameraType.THIRD_PERSON_FRONT ? CameraType.THIRD_PERSON_FRONT : CameraType.FIRST_PERSON;
        mc.options.setCameraType(cameraType);


        if (Config.Client.DISABLE_POST_EFFECT.get()) {
            Minecraft.getInstance().gameRenderer.effectActive = false;
        }
    }

    @Override
    public void imageTaken(Capture capture, NativeImage screenshot) {
        Minecraft mc = Minecraft.getInstance();
        mc.options.hideGui = hideGuiBeforeCapture;
        mc.options.setCameraType(cameraTypeBeforeCapture);

        if (Config.Client.DISABLE_POST_EFFECT.get()) {
            Minecraft.getInstance().gameRenderer.effectActive = postEffectActiveBeforeCapture;
        }
    }
}
