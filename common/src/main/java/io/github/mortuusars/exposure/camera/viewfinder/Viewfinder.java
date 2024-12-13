package io.github.mortuusars.exposure.camera.viewfinder;


import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.core.camera.component.FocalRange;
import io.github.mortuusars.exposure.core.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.item.part.Setting;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class Viewfinder {
    public static final float ZOOM_STEP = 8f;
    public static final float ZOOM_PRECISE_MODIFIER = 0.25f;
    private static boolean isOpen;

    private static FocalRange focalRange = new FocalRange(18, 55);
    private static double targetFov = 90f;
    private static double currentFov = targetFov;
    private static boolean shouldRestoreFov;

    public static boolean isOpen() {
        return isOpen;
    }

    public static boolean isLookingThrough() {
        return isOpen() && (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON
                || Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT);
    }

    public static void open() {
        LocalPlayer player = Minecraft.getInstance().player;
        Preconditions.checkState(player != null, "Player should not be null");
        Preconditions.checkState(player.level().isClientSide(), "This should be called only client-side.");

        if (isOpen())
            return;

        Optional<Camera> activeCamera = CameraClient.getActiveCamera();
        if (activeCamera.isEmpty()) {
            return;
        }

        Camera camera = activeCamera.get();

        double zoomPercentage = Setting.ZOOM.getOrDefault(camera.getItemStack(), 0.0);
        focalRange = camera.getItem().getFocalRange(camera.getItemStack());
        double focalLength = Mth.map(zoomPercentage, 0.0, 1.0, focalRange.min(), focalRange.max());
        targetFov = Fov.focalLengthToFov(focalLength);

        isOpen = true;

        ViewfinderShader.update();
        ViewfinderOverlay.setup();
    }

    public static void update() {
        @Nullable LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        updateSelfieMode();
        ViewfinderShader.update();
    }

    public static void updateSelfieMode() {
        CameraClient.setSetting(Setting.SELFIE, Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT);
    }

    public static void close() {
        if (!isOpen()) {
            return;
        }

        isOpen = false;
        targetFov = Minecraft.getInstance().options.fov().get();

        ViewfinderShader.remove();
    }

    public static FocalRange getFocalRange() {
        return focalRange;
    }

    public static double getCurrentFov() {
        return currentFov;
    }

    public static float getSelfieCameraDistance() {
        return 1.75f;
    }

    public static void zoom(ZoomDirection direction, boolean precise) {
        double step = ZOOM_STEP * (1f - Mth.clamp((focalRange.min() - currentFov) / focalRange.min(), 0.3f, 1f));
        double inertia = Math.abs(targetFov - currentFov) * 0.8f;
        double change = step + inertia;

        if (precise)
            change *= ZOOM_PRECISE_MODIFIER;

        double prevFov = targetFov;

        double fov = Mth.clamp(targetFov + (direction == ZoomDirection.IN ? -change : +change),
                Fov.focalLengthToFov(focalRange.max()),
                Fov.focalLengthToFov(focalRange.min()));

        if (Math.abs(prevFov - fov) > 0.01f)
            Objects.requireNonNull(Minecraft.getInstance().player).playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get());

        targetFov = fov;

        double focalLength = Fov.fovToFocalLength(fov);

        double zoom = Mth.map(focalLength, focalRange.min(), focalRange.max(), 0.0, 1.0);
        CameraClient.setSetting(Setting.ZOOM, zoom);
    }

    public static double modifyMouseSensitivity(double sensitivity) {
        if (!isLookingThrough())
            return sensitivity;

        double modifier = Mth.clamp(1f - (Config.Client.VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER.get()
                * ((Minecraft.getInstance().options.fov().get() - currentFov) / 5f)), 0.01, 2f);
        return sensitivity * modifier;
    }

    public static boolean handleMouseScroll(double yOffset) {
        if (isLookingThrough()) {
            zoom(yOffset > 0 ? ZoomDirection.IN : ZoomDirection.OUT, false);
            return true;
        }

        return false;
    }

    public static double modifyFov(double fov) {
        if (isLookingThrough()) {
            currentFov = Mth.lerp(Math.min(0.8f * Minecraft.getInstance().getTimer().getGameTimeDeltaTicks(), 0.8f), currentFov, targetFov);
            shouldRestoreFov = true;
            return currentFov;
        }
        else if (shouldRestoreFov && Math.abs(currentFov - fov) > 0.00001) {
            currentFov = Mth.lerp(Math.min(0.95f * Minecraft.getInstance().getTimer().getGameTimeDeltaTicks(), 0.95f), currentFov, fov);
            return currentFov;
        } else {
            currentFov = fov;
            shouldRestoreFov = false;
            return fov;
        }
    }
}
