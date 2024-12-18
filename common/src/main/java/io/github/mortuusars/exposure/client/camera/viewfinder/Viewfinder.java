package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraControlsScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.item.part.Setting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Viewfinder {
    protected final Camera camera;
    protected final ViewfinderOverlay overlay;
    protected final ViewfinderShader shader;
    protected final ComponentConstructor<ViewfinderCameraControlsScreen> controlsScreenConstructor;

    protected @Nullable ViewfinderCameraControlsScreen controlsScreen;

    public Viewfinder(Camera camera,
                      ComponentConstructor<ViewfinderOverlay> overlay,
                      ComponentConstructor<ViewfinderShader> shader,
                      ComponentConstructor<ViewfinderCameraControlsScreen> controlsScreen) {
        this.camera = camera;
        this.overlay = overlay.construct(camera, this);
        this.shader = shader.construct(camera, this);
        this.controlsScreenConstructor = controlsScreen;
        CameraClient.setSetting(Setting.SELFIE, Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT);
    }

    public ViewfinderOverlay getOverlay() {
        return overlay;
    }

    public ViewfinderShader getShader() {
        return shader;
    }

    public Optional<ViewfinderCameraControlsScreen> getControlsScreen() {
        return Optional.ofNullable(controlsScreen);
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

    public double modifyFov(double original) {
        return original;
    }

    @FunctionalInterface
    public interface ComponentConstructor<T> {
        T construct(Camera camera, Viewfinder viewfinder);
    }
}
