package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.NewCamera;
import io.github.mortuusars.exposure.core.camera.FlashMode;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class FlashModeButton extends CycleButton {
    public static final WidgetSprites FLASH_MODE_OFF_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/flash_mode_off"),
            Exposure.resource("camera_controls/flash_mode_off_disabled"),
            Exposure.resource("camera_controls/flash_mode_off_highlighted"));

    public static final Component BASE_TOOLTIP = Component.translatable("gui.exposure.camera_controls.flash_mode.tooltip");

    public FlashModeButton(int x, int y, int width, int height) {
        super(x, y, width, height, FlashMode.values().length, getCurrentFlashModeIndex(), true,
                FLASH_MODE_OFF_SPRITES, createFlashModeSpriteMap(), Tooltip.create(BASE_TOOLTIP), createTooltipMap());
    }

    private static int getCurrentFlashModeIndex() {
        Optional<NewCamera> activeCamera = CameraClient.getActiveCamera();
        return activeCamera.map(newCamera -> newCamera.getItem().getFlashMode(newCamera.getItemStack()).ordinal()).orElse(0);
    }

    private static IntObjectMap<WidgetSprites> createFlashModeSpriteMap() {
        IntObjectMap<WidgetSprites> map = new IntObjectHashMap<>();

        FlashMode[] modes = FlashMode.values();
        for (int i = 0; i < modes.length; i++) {
            FlashMode mode = modes[i];
            WidgetSprites sprites = new WidgetSprites(
                    Exposure.resource("camera_controls/flashMode_" + mode.getSerializedName()),
                    Exposure.resource("camera_controls/flashMode_" + mode.getSerializedName() + "_disabled"),
                    Exposure.resource("camera_controls/flashMode_" + mode.getSerializedName() + "_highlighted"));
            map.put(i, sprites);
        }

        return map;
    }

    private static IntObjectMap<Tooltip> createTooltipMap() {
        IntObjectMap<Tooltip> map = new IntObjectHashMap<>();

        FlashMode[] modes = FlashMode.values();
        for (int i = 0; i < modes.length; i++) {
            FlashMode mode = modes[i];
            Component tooltipComponent = BASE_TOOLTIP.copy()
                    .append(CommonComponents.NEW_LINE)
                    .append(mode.translate().withStyle(ChatFormatting.GRAY));
            map.put(i, Tooltip.create(tooltipComponent));
        }

        return map;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get(),
                Objects.requireNonNull(Minecraft.getInstance().level).random.nextFloat() * 0.15f + 0.93f, 0.7f));
    }

    @Override
    public @NotNull Component getMessage() {
        return FlashMode.values()[currentIndex].translate();
    }

    @Override
    protected void onCycle() {
        CameraClient.setFlashMode(FlashMode.values()[currentIndex]);
    }
}
