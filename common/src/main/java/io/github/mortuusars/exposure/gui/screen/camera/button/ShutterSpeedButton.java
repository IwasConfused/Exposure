package io.github.mortuusars.exposure.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.camera.infrastructure.ShutterSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ShutterSpeedButton extends CycleButton {
    private final List<ShutterSpeed> shutterSpeeds;
    private final int secondaryFontColor;
    private final int mainFontColor;

    public ShutterSpeedButton(Screen screen, int x, int y, int width, int height, int u, int v, ResourceLocation texture) {
        super(screen, x, y, width, height, u, v, height, texture);

        Camera<?> camera = CameraClient.getCamera().orElseThrow();

        List<ShutterSpeed> speeds = new ArrayList<>(camera.get().getItem().getAllShutterSpeeds(camera.get().getStack()));
        Collections.reverse(speeds);
        shutterSpeeds = speeds;

        ShutterSpeed shutterSpeed = camera.get().getItem().getShutterSpeed(camera.get().getStack());
        if (!shutterSpeeds.contains(shutterSpeed)) {
            throw new IllegalStateException("Camera {" + camera.get().getStack() + "} has invalid shutter speed.");
        }

        int currentShutterSpeedIndex = 0;
        for (int i = 0; i < shutterSpeeds.size(); i++) {
            if (shutterSpeed.equals(shutterSpeeds.get(i)))
                currentShutterSpeedIndex = i;
        }

        setupButtonElements(shutterSpeeds.size(), currentShutterSpeedIndex);
        secondaryFontColor = Config.Client.getSecondaryFontColor();
        mainFontColor = Config.Client.getMainFontColor();
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_DIAL_CLICK.get(),
                Objects.requireNonNull(Minecraft.getInstance().level).random.nextFloat() * 0.05f + 0.9f + currentIndex * 0.01f, 0.7f));
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        ShutterSpeed shutterSpeed = shutterSpeeds.get(currentIndex);
        String text = shutterSpeed.toString();

        if (shutterSpeed.equals(ShutterSpeed.DEFAULT))
            text = text + "•";

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int xPos = 35 - (textWidth / 2);

        guiGraphics.drawString(font, text, getX() + xPos, getY() + 4, secondaryFontColor, false);
        guiGraphics.drawString(font, text, getX() + xPos, getY() + 3, mainFontColor, false);
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("gui.exposure.viewfinder.shutter_speed.tooltip"), mouseX, mouseY);
    }

    @Override
    protected void onCycle() {
        CameraClient.setShutterSpeed(shutterSpeeds.get(currentIndex));
    }
}
