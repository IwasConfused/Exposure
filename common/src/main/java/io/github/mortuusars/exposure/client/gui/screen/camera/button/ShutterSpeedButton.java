package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.NewCamera;
import io.github.mortuusars.exposure.core.camera.ShutterSpeed;
import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ShutterSpeedButton extends CycleButton {
    protected final List<ShutterSpeed> shutterSpeeds;
    protected final int secondaryFontColor;
    protected final int mainFontColor;

    public ShutterSpeedButton(int x, int y, int width, int height, WidgetSprites sprites) {
        super(x, y, width, height, getShutterSpeedCount(), getCurrentShutterSpeedIndex(), true,
                sprites, new IntObjectHashMap<>(),
                Tooltip.create(Component.translatable("gui.exposure.camera_controls.shutter_speed.tooltip")), new IntObjectHashMap<>());
        shutterSpeeds = getShutterSpeeds();
        secondaryFontColor = Config.Client.getSecondaryFontColor();
        mainFontColor = Config.Client.getMainFontColor();
    }

    private static List<ShutterSpeed> getShutterSpeeds() {
        Optional<NewCamera> activeCamera = CameraClient.getActiveCamera();
        if (activeCamera.isEmpty()) {
            return List.of(ShutterSpeed.DEFAULT);
        }

        NewCamera camera = activeCamera.get();
        return camera.getItem().getShutterSpeeds(camera.getItemStack());
    }

    private static int getShutterSpeedCount() {
        return getShutterSpeeds().size();
    }

    private static int getCurrentShutterSpeedIndex() {
        Optional<NewCamera> activeCamera = CameraClient.getActiveCamera();
        if (activeCamera.isEmpty()) {
            return 0;
        }

        NewCamera camera = activeCamera.get();
        List<ShutterSpeed> shutterSpeeds = camera.getItem().getShutterSpeeds(camera.getItemStack());
        ShutterSpeed shutterSpeed = camera.getItem().getShutterSpeed(camera.getItemStack());
        int index = shutterSpeeds.indexOf(shutterSpeed);
        return Math.max(index, 0);
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
        String text = shutterSpeed.getNotation().replace("1/", "");

        if (shutterSpeed.equals(ShutterSpeed.DEFAULT))
            text = text + "•";

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int xPos = width / 2 - (textWidth / 2);

        guiGraphics.drawString(font, text, getX() + xPos, getY() + 4, secondaryFontColor, false);
        guiGraphics.drawString(font, text, getX() + xPos, getY() + 3, mainFontColor, false);
    }

    @Override
    protected void onCycle() {
        CameraClient.setShutterSpeed(shutterSpeeds.get(currentIndex));
    }
}
