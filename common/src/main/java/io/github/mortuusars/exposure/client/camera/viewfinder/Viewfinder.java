package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.CameraClient;
import io.github.mortuusars.exposure.client.Minecrft;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraControlsScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.item.part.Setting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

public class Viewfinder {
    protected final BiFunction<Viewfinder, Camera, ViewfinderOverlay> overlaySupplier;
    protected final BiFunction<Viewfinder, Camera, ViewfinderShader> shaderSupplier;
    protected final BiFunction<Viewfinder, Camera, ViewfinderCameraControlsScreen> controlsScreenSupplier;

    protected @Nullable ViewfinderOverlay overlay;
    protected @Nullable ViewfinderShader shader;
    protected @Nullable ViewfinderCameraControlsScreen controls;
    protected @Nullable Camera camera;

    public Viewfinder(BiFunction<Viewfinder, Camera, ViewfinderOverlay> overlaySupplier,
                      BiFunction<Viewfinder, Camera, ViewfinderShader> shaderSupplier,
                      BiFunction<Viewfinder, Camera, ViewfinderCameraControlsScreen> controlsScreenSupplier) {
        this.overlaySupplier = overlaySupplier;
        this.shaderSupplier = shaderSupplier;
        this.controlsScreenSupplier = controlsScreenSupplier;
    }

    public Optional<ViewfinderOverlay> getOverlay() {
        return Optional.ofNullable(overlay);
    }

    public Optional<ViewfinderShader> getShader() {
        return Optional.ofNullable(shader);
    }

    public Optional<ViewfinderCameraControlsScreen> getControlsScreen() {
        return Optional.ofNullable(controls);
    }

    public void setup(Camera camera) {
        overlay = overlaySupplier.apply(this, camera);
        this.camera = camera;
        overlay.setup();
        shader = shaderSupplier.apply(this, camera);
        shader.update();

        CameraClient.setSetting(Setting.SELFIE, Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT);
    }

    public void openControlsScreen() {
        Preconditions.checkNotNull(camera, "No active camera");
        controls = controlsScreenSupplier.apply(this, camera);
        Minecrft.get().setScreen(controls);
    }

    public boolean isLookingThrough() {
        CameraType cameraType = Minecrft.options().getCameraType();
        return cameraType == CameraType.FIRST_PERSON || cameraType == CameraType.THIRD_PERSON_FRONT;
    }

    public void processShader() {
        if (shader != null && isLookingThrough()) {
            shader.update();
            shader.process();
        }
    }

    public void render() {
        if (overlay != null && isLookingThrough()) overlay.render();
    }

    public void close() {
        overlay = null;

        if (shader != null) {
            shader.close();
            shader = null;
        }

        if (Minecrft.get().screen instanceof ViewfinderCameraControlsScreen) {
            Minecrft.get().setScreen(null);
        }
    }

    public boolean keyPressed(int key, int scanCode, int action) {
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
            CameraClient.setSetting(Setting.SELFIE, Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT);
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

        if (!isLookingThrough())
            return false;

        if (!(Minecrft.get().screen instanceof CameraControlsScreen)) {
            if (ExposureClient.getCameraControlsKey().matches(key, scanCode)) {
                openControlsScreen();
                return false; // false not handle and keep moving/sneaking
            }

            if (action == InputConstants.PRESS || action == InputConstants.REPEAT) {
                if (key == InputConstants.KEY_ADD || key == InputConstants.KEY_EQUALS) {
//                    OldViewfinder.zoom(ZoomDirection.IN, false);
                    return true;
                }

                if (key == 333 /*KEY_SUBTRACT*/ || key == InputConstants.KEY_MINUS) {
//                    OldViewfinder.zoom(ZoomDirection.OUT, false);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean mouseClicked(int button, int action) {
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
}
