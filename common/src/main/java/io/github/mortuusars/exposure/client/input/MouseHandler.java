package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraControlsScreen;
import io.github.mortuusars.exposure.client.gui.viewfinder.Viewfinders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class MouseHandler {
    private static final boolean[] heldMouseButtons = new boolean[12];

    public static boolean handleMouseButtonPress(int button, int action, int modifiers) {
        if (button >= 0 && button < heldMouseButtons.length)
            heldMouseButtons[button] = action == InputConstants.PRESS;

        return Viewfinders.mouseClicked(button, action);

//        if (CameraClient.getActiveCamera().isEmpty()) {
//            return false;
//        }
//
//        if (!(Minecraft.getInstance().screen instanceof CameraControlsScreen)) {
//            if (!Config.Common.CAMERA_VIEWFINDER_ATTACK.get() && Minecraft.getInstance().options.keyAttack.matchesMouse(button))
//                return true; // Block attacks
//
//            if (ExposureClient.getCameraControlsKey().matchesMouse(button)) {
//                ClientGUI.openViewfinderControlsScreen();
//                // Do not cancel the event to keep sneaking
//            }
//            else if (Config.Client.VIEWFINDER_MIDDLE_CLICK_CONTROLS.get() && button == InputConstants.MOUSE_BUTTON_MIDDLE) {
//                ClientGUI.openViewfinderControlsScreen();
//                return true;
//            }
//        }
//
//        return false;
    }

    public static boolean isMouseButtonHeld(int button) {
        return button >= 0 && button < heldMouseButtons.length && heldMouseButtons[button];
    }
}
