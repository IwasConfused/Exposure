package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.CameraClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class MouseHandler {
    private static final boolean[] heldMouseButtons = new boolean[12];

    public static boolean isMouseButtonHeld(int button) {
        return button >= 0 && button < heldMouseButtons.length && heldMouseButtons[button];
    }

    public static boolean buttonPressed(int button, int action, int modifiers) {
        if (button >= 0 && button < heldMouseButtons.length)
            heldMouseButtons[button] = action == InputConstants.PRESS;

        return CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseClicked(button, action);
    }

    public static boolean scrolled(double amount) {
        return CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseScrolled(amount);
    }

    public static double modifySensitivity(double original) {
        return CameraClient.viewfinder() != null ? CameraClient.viewfinder().modifyMouseSensitivity(original) : original;
    }
}
