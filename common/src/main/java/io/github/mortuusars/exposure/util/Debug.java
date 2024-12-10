package io.github.mortuusars.exposure.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class Debug {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static boolean hidden;

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (hidden) {
            return;
        }

        int y = 4;
        for (int i = 0; i < 12; i++) {
            int height = renderLine(guiGraphics, deltaTracker, i, y);
            if (height > 0)
                y += height + 2;
        }
    }

    public static void onKeyPress(int key, int scanCode) {
        boolean shift = Screen.hasShiftDown();
        if (key == InputConstants.KEY_UP) up(shift);
        if (key == InputConstants.KEY_DOWN) down(shift);
        if (key == InputConstants.KEY_LEFT) left(shift);
        if (key == InputConstants.KEY_RIGHT) right(shift);
        if (key == InputConstants.KEY_NUMPAD0) toggleHidden(shift);
    }

    public static void onKeyRelease(int key, int scanCode) {

    }


    // --
    private static int renderLine(GuiGraphics guiGraphics, DeltaTracker deltaTracker, int line, int yPos) {
        if (line == 0 && minecraft.level != null) {
            return renderString(guiGraphics, "Game Time: " + minecraft.level.getGameTime(), yPos);
        }
        if (line == 1) {
            return renderString(guiGraphics, "Lorem ipsum odor amet, consectetuer adipiscing elit. Venenatis quam at; ullamcorper duis ex felis. Facilisi mollis scelerisque penatibus habitant turpis adipiscing. Fermentum neque condimentum at convallis dolor nibh! Diam nec class praesent tempor quam rhoncus parturient finibus. Libero varius imperdiet hac nunc viverra efficitur nascetur. Tellus finibus eleifend inceptos sociosqu platea laoreet integer egestas. Condimentum dictum dui torquent dictum velit nam posuere curae? Ex orci nam fringilla integer praesent imperdiet volutpat pharetra fames.\n" +
                            "\n" +
                            "Magnis aptent est tellus tortor nascetur ligula a orci. Dictum posuere amet at massa id gravida lorem ligula. Nunc ad fusce magnis finibus eleifend est nostra sociosqu. Tempor dapibus morbi ornare malesuada himenaeos egestas curabitur enim! Mattis vulputate facilisis ligula, felis maximus integer. ",
                    yPos);
        }
        if (line == 3) {
            return renderString(guiGraphics, Integer.toString(count), yPos);
        }
        return 0;
    }

    private static int renderString(GuiGraphics guiGraphics, String text, int yPos) {
        List<FormattedCharSequence> lines = minecraft.font.split(FormattedText.of(text), 400);
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            guiGraphics.drawString(minecraft.font, line, 4, yPos + (i * (10)), 0xFFFFFFFF);
        }
        return lines.size() * 10;
    }

    private static int count = 0;

    private static void up(boolean shift) {
        count++;
    }

    private static void down(boolean shift) {
        count--;
    }

    private static void left(boolean shift) {
    }

    private static void right(boolean shift) {
    }

    private static void toggleHidden(boolean shift) {
        hidden = !hidden;
    }
}
