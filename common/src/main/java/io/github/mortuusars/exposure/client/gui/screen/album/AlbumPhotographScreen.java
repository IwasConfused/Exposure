package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlbumPhotographScreen extends PhotographScreen {
    private final Screen parentScreen;

    public AlbumPhotographScreen(Screen parentScreen, List<ItemAndStack<PhotographItem>> photographs) {
        super(photographs);
        this.parentScreen = parentScreen;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (zoom.get() < zoom.getMin() + 0.1f && zoom.getTarget() < zoom.getMin() + 0.1f) {
            onClose();
            return;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE || Minecrft.options().keyInventory.matches(keyCode, scanCode)) {
            zoom.setTarget(0f);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            zoom.setTarget(0f);
            return true;
        }

        return false;
    }

    @Override
    public void onClose() {
        Minecrft.get().setScreen(parentScreen);
        Minecrft.player().playSound(Exposure.SoundEvents.PHOTOGRAPH_PLACE.get(), 0.7f, 1.1f);
    }
}
