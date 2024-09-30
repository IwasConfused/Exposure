package io.github.mortuusars.exposure.client.gui.component;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.gui.Tooltips;
import io.github.mortuusars.exposure.client.gui.Widgets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CycleButton<T> extends Button {
    protected final List<T> values;
    protected final T startingValue;
    protected final Map<T, WidgetSprites> spritesMap;
    protected final OnCycle<T> onCycle;

    protected boolean loop = true;
    protected @Nullable Tooltip defaultTooltip;
    protected Map<T, @Nullable Tooltip> tooltips = Collections.emptyMap();
    protected @Nullable SoundEvent soundEvent;

    protected int currentIndex;

    public CycleButton(int x, int y, int width, int height, List<T> values, @NotNull T startingValue,
                       Map<T, WidgetSprites> spritesMap, OnCycle<T> onCycle) {
        super(x, y, width, height, CommonComponents.EMPTY, b -> {}, DEFAULT_NARRATION);
        Preconditions.checkArgument(!values.isEmpty(), "Cannot create a CycleButton with 0 possible values.");
        this.values = values;
        this.startingValue = startingValue;
        Preconditions.checkArgument(!spritesMap.isEmpty(), "Cannot create a CycleButton with 0 sprites.");
        this.spritesMap = spritesMap;
        this.onCycle = onCycle;

        this.currentIndex = Math.max(values.indexOf(startingValue), 0);
    }

    public CycleButton(int x, int y, int width, int height, List<T> values, @NotNull T startingValue,
                       Function<T, WidgetSprites> spritesFunc, OnCycle<T> onCycle) {
        this(x, y, width, height, values, startingValue, Widgets.createMap(values, spritesFunc), onCycle);
    }

    public CycleButton<T> setLooping(boolean loop) {
        this.loop = loop;
        return this;
    }

    public CycleButton<T> setDefaultTooltip(Tooltip tooltip) {
        this.defaultTooltip = tooltip;
        return this;
    }

    public CycleButton<T> setTooltips(Map<T, Tooltip> tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    public CycleButton<T> setTooltips(Function<T, Component> tooltipFunc) {
        this.tooltips = Tooltips.createMap(values, tooltipFunc);
        return this;
    }

    public CycleButton<T> setClickSound(SoundEvent soundEvent) {
        this.soundEvent = soundEvent;
        return this;
    }

    public T getCurrentValue() {
        return values.get(Mth.clamp(currentIndex, 0, values.size() - 1));
    }

    public void setCurrentValue(T value) {
        setCurrentIndex(values.indexOf(value));
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = Mth.clamp(currentIndex, 0, values.size() - 1);
        updateVisuals();
    }

    public void cycle(boolean reverse) {
        int value = currentIndex;
        value += reverse ? -1 : 1;
        if (value < 0)
            value = loop ? values.size() - 1 : 0;
        else if (value >= values.size())
            value = loop ? 0 : values.size() - 1;

        if (currentIndex != value) {
            currentIndex = value;
            onCycle();
        }
    }

    protected void onCycle() {
        updateVisuals();
        onCycle.onCycle(this, getCurrentValue());
    }

    private void updateVisuals() {
        setTooltip(tooltips.getOrDefault(getCurrentValue(), defaultTooltip));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        @Nullable WidgetSprites sprites = spritesMap.get(getCurrentValue());
        ResourceLocation spriteLocation = sprites != null
                ? sprites.get(isActive(), isHoveredOrFocused())
                : TextureManager.INTENTIONAL_MISSING_TEXTURE;
        guiGraphics.blitSprite(spriteLocation, getX(), getY(), getWidth(), getHeight());
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

    @Override
    public void playDownSound(SoundManager handler) {
        if (soundEvent != null) {
            handler.play(SimpleSoundInstance.forUI(soundEvent, 1.0F));
        }
        else {
            handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    public interface OnCycle<T> {
        void onCycle(CycleButton<T> button, T newValue);
    }
}
