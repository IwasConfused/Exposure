package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraControlsScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public class KeyboardHandler {
    public static boolean handleKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        return handleCameraKeyPress(key, scanCode, action);
    }

    private static boolean handleCameraKeyPress(int key, int scanCode, int action) {
        Minecraft minecraft = Minecraft.getInstance();
        @Nullable LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }

        if (CameraClient.getActiveCamera().isEmpty()) {
            return false;
        }

        if (!Config.Common.CAMERA_VIEWFINDER_ATTACK.get()
                && Minecraft.getInstance().options.keyAttack.matches(key, scanCode)
                && !(Minecraft.getInstance().screen instanceof CameraControlsScreen)) {
            return true;
        }

        if (minecraft.options.keyTogglePerspective.matches(key, scanCode)) {
            if (action == InputConstants.PRESS)
                return true;

            CameraType currentCameraType = minecraft.options.getCameraType();
            CameraType newCameraType = currentCameraType == CameraType.FIRST_PERSON ? CameraType.THIRD_PERSON_FRONT
                    : CameraType.FIRST_PERSON;

            minecraft.options.setCameraType(newCameraType);
            return true;
        }

        if (key == InputConstants.KEY_ESCAPE || minecraft.options.keyInventory.matches(key, scanCode)) {
            if (action == InputConstants.PRESS) { // TODO: Check if activating on release is not causing problems
                if (minecraft.screen instanceof CameraControlsScreen viewfinderControlsScreen) {
                    viewfinderControlsScreen.onClose();
                } else {
                    CameraClient.deactivateCameraAndSendToServer();
                }
            }
            return true;
        }

        if (!Viewfinder.isLookingThrough())
            return false;

        if (!(minecraft.screen instanceof CameraControlsScreen)) {
            if (ExposureClient.getCameraControlsKey().matches(key, scanCode)) {
                ClientGUI.openViewfinderControlsScreen();
                return false;
            }

            if (action == 1 || action == 2) { // Press or Hold
                if (key == InputConstants.KEY_ADD || key == InputConstants.KEY_EQUALS) {
                    Viewfinder.zoom(ZoomDirection.IN, false);
                    return true;
                }

                if (key == 333 /*KEY_SUBTRACT*/ || key == InputConstants.KEY_MINUS) {
                    Viewfinder.zoom(ZoomDirection.OUT, false);
                    return true;
                }
            }
        }

        return false;
    }
}
