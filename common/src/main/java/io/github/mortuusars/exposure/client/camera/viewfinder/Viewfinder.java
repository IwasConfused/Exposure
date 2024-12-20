package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraControlsScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.item.part.Setting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Viewfinder {
    protected final Camera camera;
    protected final ViewfinderZoom zoom;
    protected final ViewfinderOverlay overlay;
    protected final ViewfinderShader shader;
    protected final ComponentConstructor<ViewfinderCameraControlsScreen> controlsScreenConstructor;

    protected @Nullable ViewfinderCameraControlsScreen controlsScreen;

    public Viewfinder(Camera camera,
                      ComponentConstructor<ViewfinderZoom> zoom,
                      ComponentConstructor<ViewfinderOverlay> overlay,
                      ComponentConstructor<ViewfinderShader> shader,
                      ComponentConstructor<ViewfinderCameraControlsScreen> controlsScreen) {
        this.camera = camera;
        this.zoom = zoom.construct(camera, this);
        this.overlay = overlay.construct(camera, this);
        this.shader = shader.construct(camera, this);
        this.controlsScreenConstructor = controlsScreen;
    }

    public Camera camera() {
        return camera;
    }

    public ViewfinderZoom zoom() {
        return zoom;
    }

    public ViewfinderOverlay overlay() {
        return overlay;
    }

    public ViewfinderShader shader() {
        return shader;
    }

    public Optional<ViewfinderCameraControlsScreen> getControlsScreen() {
        return Optional.ofNullable(controlsScreen);
    }

    public void tick() {
    }

    public void close() {
        if (shader != null) {
            shader.close();
        }

        if (Minecrft.get().screen instanceof ViewfinderCameraControlsScreen) {
            Minecrft.get().setScreen(null);
        }
    }

    public void openControlsScreen() {
        Preconditions.checkNotNull(camera, "No active camera");
        controlsScreen = controlsScreenConstructor.construct(camera, this);
        Minecrft.get().setScreen(controlsScreen);
    }

    public boolean isLookingThrough() {
        CameraType cameraType = Minecrft.options().getCameraType();
        return cameraType == CameraType.FIRST_PERSON || cameraType == CameraType.THIRD_PERSON_FRONT;
    }

    public float getMaxSelfieCameraDistance() {
        return 1.75f;
    }

    public boolean keyPressed(int key, int scanCode, int action) {
        if (!isLookingThrough()) {
            return false;
        }

        if (!Config.Common.CAMERA_VIEWFINDER_ATTACK.get()
                && Minecrft.options().keyAttack.matches(key, scanCode)
                && !(Minecrft.get().screen instanceof CameraControlsScreen)) {
            return true;
        }

        if (Minecrft.options().keyTogglePerspective.matches(key, scanCode)) {
            if (action == InputConstants.PRESS)
                return true;

            CameraType currentCameraType = Minecrft.options().getCameraType();
            CameraType newCameraType = currentCameraType == CameraType.FIRST_PERSON ? CameraType.THIRD_PERSON_FRONT
                    : CameraType.FIRST_PERSON;

            Minecrft.options().setCameraType(newCameraType);
            CameraClient.setSetting(Setting.SELFIE_MODE, Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT);
            return true;
        }

        if (key == InputConstants.KEY_ESCAPE || Minecrft.options().keyInventory.matches(key, scanCode)) {
            if (action == InputConstants.PRESS) {
                if (Minecrft.get().screen instanceof ViewfinderCameraControlsScreen viewfinderControlsScreen) {
                    viewfinderControlsScreen.onClose();
                } else {
                    CameraClient.deactivate();
                    close();
                }
            }
            return true;
        }

        if (!(Minecrft.get().screen instanceof ViewfinderCameraControlsScreen)) {
            if (ExposureClient.getCameraControlsKey().matches(key, scanCode)) {
                openControlsScreen();
                return false; // false not handle and keep moving/sneaking
            }

            if (zoom.keyPressed(key, scanCode, action)) {
                return true;
            }
        }

        return false;
    }

    public boolean mouseClicked(int button, int action) {
        if (!isLookingThrough()) {
            return false;
        }

        if (Minecrft.get().screen instanceof ViewfinderCameraControlsScreen) return false;

        if (!Config.Common.CAMERA_VIEWFINDER_ATTACK.get() && Minecrft.options().keyAttack.matchesMouse(button))
            return true; // Block attacks

        if (ExposureClient.getCameraControlsKey().matchesMouse(button)) {
            openControlsScreen();
            return false; // Do not cancel the event to keep sneaking
        }

        if (Config.Client.VIEWFINDER_MIDDLE_CLICK_CONTROLS.get() && button == InputConstants.MOUSE_BUTTON_MIDDLE) {
            openControlsScreen();
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double amount) {
        return isLookingThrough() && !(Minecrft.get().screen instanceof CameraControlsScreen) && zoom.mouseScrolled(amount);
    }

    public double modifyMouseSensitivity(double original) {
        if (!isLookingThrough())
            return original;

        double scale = original / Minecraft.getInstance().options.fov().get();
        double scaledSensitivity = zoom.getCurrentFov() * scale;

        double normalizedDifference = Mth.map(original - scaledSensitivity, 0, original, 0, 1);
        double influence = Config.Client.VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE.get();
        double strength = 1f - normalizedDifference * influence;
        strength *= strength; // more influence at smaller FOVs

        return Mth.lerp(strength, scaledSensitivity, original);
    }

    @FunctionalInterface
    public interface ComponentConstructor<T> {
        T construct(Camera camera, Viewfinder viewfinder);
    }
}
