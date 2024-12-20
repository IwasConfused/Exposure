package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class ShutterSpeedButton extends CycleButton<ShutterSpeed> {
    protected final int secondaryFontColor;
    protected final int mainFontColor;

    public ShutterSpeedButton(int x, int y, int width, int height, List<ShutterSpeed> values, @NotNull ShutterSpeed startingValue,
                              Function<ShutterSpeed, WidgetSprites> spritesFunc, OnCycle<ShutterSpeed> onCycle) {
        super(x, y, width, height, values, startingValue, spritesFunc, onCycle);
        mainFontColor = Config.Client.getMainFontColor();
        secondaryFontColor = Config.Client.getSecondaryFontColor();
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (clickSound != null) {
            handler.play(SimpleSoundInstance.forUI(clickSound,
                    ThreadLocalRandom.current().nextFloat() * 0.05f + 0.9f + currentIndex * 0.01f, 0.7f));
        }
        else {
            super.playDownSound(handler);
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        ShutterSpeed shutterSpeed = getCurrentValue();
        String text = shutterSpeed.getNotation().replace("1/", "");

        if (shutterSpeed.equals(ShutterSpeed.DEFAULT))
            text = text + "•";

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int xPos = width / 2 - (textWidth / 2) + 1;

        guiGraphics.drawString(font, text, getX() + xPos, getY() + 4, secondaryFontColor, false);
        guiGraphics.drawString(font, text, getX() + xPos, getY() + 3, mainFontColor, false);
    }
}
