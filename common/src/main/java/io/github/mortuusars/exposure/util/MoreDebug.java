package io.github.mortuusars.exposure.util;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.mixin.MoreDebugScreenRenderLinesInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collections;
import java.util.List;

public class MoreDebug {
    public static boolean isOnMoreDebugPage;

    public static boolean onKeyPress(int key, int scanCode) {
        boolean shift = Screen.hasShiftDown();
        if (key == InputConstants.KEY_UP) up(shift);
        if (key == InputConstants.KEY_DOWN) down(shift);
        if (key == InputConstants.KEY_LEFT) isOnMoreDebugPage = false;
        if (key == InputConstants.KEY_RIGHT) isOnMoreDebugPage = true;
        return false;
    }

    public static boolean onKeyRelease(int key, int scanCode) {
        return false;
    }

    static int count;

    private static void up(boolean shift) {
        count++;
    }

    private static void down(boolean shift) {
        count--;
    }

    public static void render(GuiGraphics guiGraphics) {
        List<String> leftLines = collectLeftLines();
        ((MoreDebugScreenRenderLinesInvoker) Minecraft.getInstance().getDebugOverlay()).drawLines(guiGraphics, leftLines, true);
        List<String> rightLines = collectRightLines();
        ((MoreDebugScreenRenderLinesInvoker) Minecraft.getInstance().getDebugOverlay()).drawLines(guiGraphics, rightLines, true);
    }

    private static List<String> collectLeftLines() {
        return List.of(
                Integer.toString(count)
        );
    }

    private static List<String> collectRightLines() {
        return Collections.emptyList();
    }
}
