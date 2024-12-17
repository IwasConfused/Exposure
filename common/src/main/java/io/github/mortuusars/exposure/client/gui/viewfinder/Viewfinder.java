package io.github.mortuusars.exposure.client.gui.viewfinder;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.client.MC;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraControlsScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.core.camera.NewCamera;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.common.DeactivateActiveCameraCommonPacket;
import net.minecraft.client.CameraType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

public class Viewfinder {
    protected final BiFunction<Viewfinder, NewCamera, ViewfinderOverlay> overlaySupplier;
    protected final BiFunction<Viewfinder, NewCamera, ViewfinderShader> shaderSupplier;
    protected final BiFunction<Viewfinder, NewCamera, ViewfinderCameraControlsScreen> controlsScreenSupplier;

    protected @Nullable ViewfinderOverlay overlay;
    protected @Nullable ViewfinderShader shader;
    protected @Nullable ViewfinderCameraControlsScreen controls;
    protected @Nullable NewCamera camera;

    public Viewfinder(BiFunction<Viewfinder, NewCamera, ViewfinderOverlay> overlaySupplier,
                      BiFunction<Viewfinder, NewCamera, ViewfinderShader> shaderSupplier,
                      BiFunction<Viewfinder, NewCamera, ViewfinderCameraControlsScreen> controlsScreenSupplier) {
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

    public void setup(NewCamera camera) {
        overlay = overlaySupplier.apply(this, camera);
        this.camera = camera;
        overlay.setup();
        shader = shaderSupplier.apply(this, camera);
        shader.update();
    }

    public void openControlsScreen() {
        Preconditions.checkNotNull(camera, "No active camera");
        controls = controlsScreenSupplier.apply(this, camera);
        MC.get().setScreen(controls);
    }

    public boolean isLookingThrough() {
        CameraType cameraType = MC.options().getCameraType();
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

        if (MC.get().screen instanceof ViewfinderCameraControlsScreen) {
            MC.get().setScreen(null);
        }
    }

    public boolean keyPressed(int key, int scanCode, int action) {
        if (!Config.Common.CAMERA_VIEWFINDER_ATTACK.get()
                && MC.options().keyAttack.matches(key, scanCode)
                && !(MC.get().screen instanceof CameraControlsScreen)) {
            return true;
        }

        if (MC.options().keyTogglePerspective.matches(key, scanCode)) {
            if (action == InputConstants.PRESS)
                return true;

            CameraType currentCameraType = MC.options().getCameraType();
            CameraType newCameraType = currentCameraType == CameraType.FIRST_PERSON ? CameraType.THIRD_PERSON_FRONT
                    : CameraType.FIRST_PERSON;

            MC.options().setCameraType(newCameraType);
            return true;
        }

        if (key == InputConstants.KEY_ESCAPE || MC.options().keyInventory.matches(key, scanCode)) {
            if (action == InputConstants.PRESS) {
                if (MC.get().screen instanceof ViewfinderCameraControlsScreen viewfinderControlsScreen) {
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

        if (!(MC.get().screen instanceof CameraControlsScreen)) {
            if (ExposureClient.getCameraControlsKey().matches(key, scanCode)) {
                openControlsScreen();
                return false; // false not handle and keep moving/sneaking
            }

            if (action == 1 || action == 2) { // Press or Hold
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
        if (MC.get().screen instanceof ViewfinderCameraControlsScreen) return false;

        if (!Config.Common.CAMERA_VIEWFINDER_ATTACK.get() && MC.options().keyAttack.matchesMouse(button))
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
