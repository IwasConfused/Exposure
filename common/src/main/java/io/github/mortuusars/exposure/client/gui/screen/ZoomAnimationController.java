package io.github.mortuusars.exposure.client.gui.screen;

import io.github.mortuusars.exposure.core.camera.ZoomDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class ZoomAnimationController {
    protected float step = 1.35f;
    protected float defaultZoom = 1.0f;

    protected float min = defaultZoom / (float)Math.pow(step, 4f);
    protected float max = defaultZoom * (float)Math.pow(step, 4f);
    protected float targetZoom = defaultZoom;
    protected float currentZoom = 0.1f;

    protected float zoomInSpeed = 0.8f;
    protected float zoomOutSpeed = 1f;

    public void update() {
        float speed = currentZoom < targetZoom ? zoomInSpeed : zoomOutSpeed;
        float delta = speed * Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        set(Mth.lerp(delta, currentZoom, targetZoom));
    }

    public void change(ZoomDirection direction) {
        setTarget(direction == ZoomDirection.IN ? targetZoom * step : targetZoom / step);
    }

    public void setTarget(float target) {
        targetZoom = Mth.clamp(target, min, max);
    }

    public void set(float actual) {
        currentZoom = Mth.clamp(actual, min, max);
    }

    public float get() {
        return currentZoom;
    }
}
