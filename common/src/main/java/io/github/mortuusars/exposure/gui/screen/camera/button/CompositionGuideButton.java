package io.github.mortuusars.exposure.gui.screen.camera.button;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.NewCamera;
import io.github.mortuusars.exposure.core.camera.CompositionGuide;
import io.github.mortuusars.exposure.core.camera.CompositionGuides;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CompositionGuideButton extends CycleButton {
    public static final WidgetSprites GUIDE_NONE_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/composition_guide/none"),
            Exposure.resource("camera_controls/composition_guide/none_disabled"),
            Exposure.resource("camera_controls/composition_guide/none_highlighted"));

    public static final Component BASE_TOOLTIP = Component.translatable("gui.exposure.camera_controls.composition_guide.tooltip");

    private final List<CompositionGuide> guides;

    public CompositionGuideButton(int x, int y, int width, int height) {
        super(x, y, width, height, CompositionGuides.getGuides().size(), getCurrentGuideIndex(), true,
                GUIDE_NONE_SPRITES, createGuideSpriteMap(), Tooltip.create(BASE_TOOLTIP), createTooltipMap());
        guides = CompositionGuides.getGuides();
    }

    private static int getCurrentGuideIndex() {
        Optional<NewCamera> activeCamera = CameraClient.getActiveCamera();
        if (activeCamera.isEmpty()) {
            return 0;
        }

        CompositionGuide guide = activeCamera.get().getItem().getCompositionGuide(activeCamera.get().getItemStack());
        List<CompositionGuide> guides = CompositionGuides.getGuides();
        int index = guides.indexOf(guide);
        return Math.max(index, 0);
    }

    private static IntObjectMap<WidgetSprites> createGuideSpriteMap() {
        IntObjectMap<WidgetSprites> map = new IntObjectHashMap<>();

        List<CompositionGuide> guides = CompositionGuides.getGuides();
        for (int i = 0; i < guides.size(); i++) {
            CompositionGuide guide = guides.get(i);
            WidgetSprites sprites = new WidgetSprites(
                    Exposure.resource("camera_controls/composition_guide/" + guide.name()),
                    Exposure.resource("camera_controls/composition_guide/" + guide.name() + "_disabled"),
                    Exposure.resource("camera_controls/composition_guide/" + guide.name() + "_highlighted"));
            map.put(i, sprites);
        }

        return map;
    }

    private static IntObjectMap<Tooltip> createTooltipMap() {
        IntObjectMap<Tooltip> map = new IntObjectHashMap<>();

        List<CompositionGuide> guides = CompositionGuides.getGuides();
        for (int i = 0; i < guides.size(); i++) {
            CompositionGuide guide = guides.get(i);
            Component tooltipComponent = BASE_TOOLTIP.copy()
                    .append(CommonComponents.NEW_LINE)
                    .append(guide.translate().withStyle(ChatFormatting.GRAY));
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
        return guides.get(currentIndex).translate();
    }

    @Override
    protected void onCycle() {
        CameraClient.setCompositionGuide(guides.get(currentIndex));
    }
}
