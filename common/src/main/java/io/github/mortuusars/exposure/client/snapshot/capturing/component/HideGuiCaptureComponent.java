package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import net.minecraft.client.Minecraft;

public class HideGuiCaptureComponent implements CaptureComponent {
    private boolean hideGuiBeforeCapture;

    @Override
    public void beforeCapture() {
        hideGuiBeforeCapture = Minecraft.getInstance().options.hideGui;
        Minecraft.getInstance().options.hideGui = true;
    }

    @Override
    public void afterCapture() {
        Minecraft.getInstance().options.hideGui = hideGuiBeforeCapture;
    }
}
