package io.github.mortuusars.exposure.client.snapshot.capturing.component;

import net.minecraft.client.Minecraft;

public class DisablePostEffectComponent implements CaptureComponent {
    private boolean effectActive;

    @Override
    public void beforeCapture() {
        effectActive = Minecraft.getInstance().gameRenderer.effectActive;
        Minecraft.getInstance().gameRenderer.effectActive = false;
    }

    @Override
    public void afterCapture() {
        Minecraft.getInstance().gameRenderer.effectActive = effectActive;
    }
}
