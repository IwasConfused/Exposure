package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.core.camera.component.FocalRange;
import io.github.mortuusars.exposure.core.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.part.CameraSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

public class ViewfinderZoom {
    public static final float ZOOM_STEP = 8f;
    public static final float ZOOM_PRECISE_MODIFIER = 0.25f;

    protected final Camera camera;
    protected final Viewfinder viewfinder;
    protected final FocalRange focalRange;

    protected Animation animation;
    protected double targetFov;
    protected double currentFov;

    public ViewfinderZoom(Camera camera, Viewfinder viewfinder) {
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.focalRange = camera.map((cameraItem, cameraStack) -> cameraItem.getFocalRange(Minecrft.registryAccess(), cameraStack))
                .orElse(FocalRange.getDefault());

        animation = new Animation(300, EasingFunction.EASE_OUT_EXPO);

        double defaultFov = Minecrft.options().fov().get();
        currentFov = defaultFov;
        targetFov = camera.map(CameraSetting.ZOOM::getOrDefault)
                .map(focalRange::fovFromZoom)
                .orElse(defaultFov);
    }

    public double getCurrentFov() {
        return Mth.lerp(animation.getValue(), currentFov, targetFov);
    }

    public void zoom(ZoomDirection direction, boolean precise) {
        currentFov = getCurrentFov();

        double step = ZOOM_STEP * (1f - Mth.clamp((focalRange.min() - currentFov) / focalRange.min(), 0.3f, 1f));
        double inertia = Math.abs(targetFov - currentFov) * 0.8f; // Faster zoom if mouse scrolled rapidly.
        double change = step + inertia;
        if (precise) {
            change *= ZOOM_PRECISE_MODIFIER;
        }

        double prevFov = targetFov;

        double fov = focalRange.clampFov(targetFov + (direction == ZoomDirection.IN ? -change : +change));

        if (Math.abs(prevFov - fov) > 0.0001) {
            Minecrft.player().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get());

            targetFov = fov;
            animation.resetProgress();

            CameraClient.setSetting(CameraSetting.ZOOM, (float)focalRange.zoomFromFov(fov));
        }
    }

    public boolean keyPressed(int key, int scanCode, int action) {
        if (action == InputConstants.PRESS || action == InputConstants.REPEAT) {
            if (key == InputConstants.KEY_ADD || key == InputConstants.KEY_EQUALS) {
                zoom(ZoomDirection.IN, Screen.hasShiftDown());
                return true;
            }

            if (key == 333 /*KEY_SUBTRACT*/ || key == InputConstants.KEY_MINUS) {
                zoom(ZoomDirection.OUT, Screen.hasShiftDown());
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double amount) {
        zoom(amount > 0 ? ZoomDirection.IN : ZoomDirection.OUT, Screen.hasShiftDown());
        return true;
    }
}
