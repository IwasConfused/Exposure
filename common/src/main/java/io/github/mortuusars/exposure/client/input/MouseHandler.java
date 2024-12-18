package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.client.CameraClient;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderRegistry;

public class MouseHandler {
    private static final boolean[] heldMouseButtons = new boolean[12];

    public static boolean handleMouseButtonPress(int button, int action, int modifiers) {
        if (button >= 0 && button < heldMouseButtons.length)
            heldMouseButtons[button] = action == InputConstants.PRESS;

        return CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseClicked(button, action);
    }

    public static boolean isMouseButtonHeld(int button) {
        return button >= 0 && button < heldMouseButtons.length && heldMouseButtons[button];
    }
}
