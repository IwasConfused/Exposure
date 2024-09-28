package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.netty.util.collection.IntObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CycleButton extends ImageButton {
    protected final int indexCount;
    protected final boolean loop;
    protected final Map<Integer, WidgetSprites> spritesMap;
    protected final Tooltip defaultTooltip;
    protected final Map<Integer, Tooltip> tooltipMap;

    protected int currentIndex;

    public CycleButton(int x, int y, int width, int height, int indexCount, int startingIndex, boolean loop,
                       WidgetSprites defaultSprites, IntObjectMap<WidgetSprites> spritesMap,
                       @Nullable Tooltip defaultTooltip, IntObjectMap<Tooltip> tooltipMap) {
        super(x, y, width, height, defaultSprites, button -> {});
        this.indexCount = indexCount;
        this.loop = loop;
        this.spritesMap = spritesMap;
        this.defaultTooltip = defaultTooltip;
        this.tooltipMap = tooltipMap;

        this.currentIndex = startingIndex;
    }

    public int getIndexCount() {
        return indexCount;
    }

    public boolean isLooping() {
        return loop;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setTooltip(tooltipMap.getOrDefault(getCurrentIndex(), defaultTooltip));

        WidgetSprites sprites = spritesMap.getOrDefault(getCurrentIndex(), this.sprites);
        ResourceLocation resourceLocation = sprites.get(this.isActive(), this.isHoveredOrFocused());
        guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && clicked(mouseX, mouseY)) {
            cycle(button == 1);
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        cycle(scrollY < 0d);
        playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean pressed = super.keyPressed(pKeyCode, pScanCode, pModifiers);

        if (pressed)
            cycle(Screen.hasShiftDown());

        return pressed;
    }

    protected void cycle(boolean reverse) {
        int value = currentIndex;
        value += reverse ? -1 : 1;
        if (value < 0)
            value = loop ? indexCount - 1 : 0;
        else if (value >= indexCount)
            value = loop ? 0 : indexCount - 1;

        if (currentIndex != value) {
            currentIndex = value;
            onCycle();
        }
    }

    protected void onCycle() {

    }
}
