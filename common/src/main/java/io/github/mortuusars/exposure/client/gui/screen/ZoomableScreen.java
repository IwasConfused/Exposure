package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import io.github.mortuusars.exposure.client.gui.screen.element.ZoomHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class ZoomableScreen extends Screen {
    protected final ZoomHandler zoom = new ZoomHandler();
    protected float zoomFactor = 1f;
    protected float scale = 1f;
    protected float x;
    protected float y;

    @NotNull
    protected final Minecraft minecraft = Minecraft.getInstance();

    protected ZoomableScreen(Component title) {
        super(title);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        zoom.update(partialTick);
        scale = zoom.get() * zoomFactor;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
        } else if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS) {
            zoom.change(ZoomDirection.IN);
        } else if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS) {
            zoom.change(ZoomDirection.OUT);
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }

        if (scrollY != 0) {
            zoom.change(scrollY >= 0.0 ? ZoomDirection.IN : ZoomDirection.OUT);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        if (button == 0) { // Left Click
            float centerX = width / 2f;
            float centerY = height / 2f;

            x = (float) Mth.clamp(x + dragX, -centerX, centerX);
            y = (float) Mth.clamp(y + dragY, -centerY, centerY);

            return true;
        }

        return false;
    }
}
