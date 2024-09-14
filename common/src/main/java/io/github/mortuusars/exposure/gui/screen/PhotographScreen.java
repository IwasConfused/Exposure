package io.github.mortuusars.exposure.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.core.frame.FrameProperties;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.warehouse.client.ClientsideExposureExporter;
import io.github.mortuusars.exposure.gui.screen.element.Pager;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.client.render.PhotographRenderProperties;
import io.github.mortuusars.exposure.client.render.PhotographRenderer;
import io.github.mortuusars.exposure.util.ClientsideWorldNameGetter;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PhotographScreen extends ZoomableScreen {
    public static final ResourceLocation WIDGETS_TEXTURE = Exposure.resource("textures/gui/widgets.png");

    public static final WidgetSprites PREV_BUTTON_SPRITES = new WidgetSprites(
            Exposure.resource("widgets/previous_button"),
            Exposure.resource("widgets/previous_button_disabled"),
            Exposure.resource("widgets/previous_button_highlighted"));

    public static final WidgetSprites NEXT_BUTTON_SPRITES = new WidgetSprites(
            Exposure.resource("widgets/next_button"),
            Exposure.resource("widgets/next_button_disabled"),
            Exposure.resource("widgets/next_button_highlighted"));

    private final List<ItemAndStack<PhotographItem>> photographs;
    private final List<String> savedExposures = new ArrayList<>();

    private final Pager pager = new Pager(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get());

    public PhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        super(Component.empty());
        Preconditions.checkState(!photographs.isEmpty(), "No photographs to display.");
        this.photographs = photographs;

        if (shouldQueryAllPhotographsImmediately()) {
            queryAllPhotographs(photographs);
        }
    }

    protected boolean shouldQueryAllPhotographsImmediately() {
        return true;
    }

    protected void queryAllPhotographs(List<ItemAndStack<PhotographItem>> photographs) {
        for (ItemAndStack<PhotographItem> photograph : photographs) {
            ExposureFrame frame = photograph.getItem().getFrame(photograph.getItemStack());
            frame.identifier().ifId(ExposureClient::getOrQuery);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        zoomFactor = (float) height / ExposureClient.exposureRenderer().getSize();

        ImageButton previousButton = new ImageButton(0, (int) (height / 2f - 16 / 2f), 16, 16,
                PREV_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        addRenderableWidget(previousButton);

        ImageButton nextButton = new ImageButton(width - 16, (int) (height / 2f - 16 / 2f), 16, 16,
                NEXT_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        addRenderableWidget(nextButton);

        pager.init(photographs.size(), true, previousButton, nextButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        pager.update();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 500); // Otherwise exposure will overlap buttons
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().translate(width / 2f, height / 2f, 0);
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.pose().translate(ExposureClient.exposureRenderer().getSize() / -2f, ExposureClient.exposureRenderer().getSize() / -2f, 0);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        ArrayList<ItemAndStack<PhotographItem>> photos = new ArrayList<>(photographs);
        Collections.rotate(photos, -pager.getCurrentPage());
        PhotographRenderer.renderStackedPhotographs(photos, guiGraphics.pose(), bufferSource, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);

        bufferSource.endBatch();

        guiGraphics.pose().popPose();

        ItemAndStack<PhotographItem> photograph = photographs.get(pager.getCurrentPage());

        renderFrameInfoHint(guiGraphics, mouseX, mouseY, photograph);

        if (Config.Client.SAVE_EXPOSURE_TO_FILE_WHEN_VIEWED.get())
            trySaveToFile(photograph);
    }

    private void renderFrameInfoHint(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, ItemAndStack<PhotographItem> photograph) {
        if (minecraft.player == null || !minecraft.player.isCreative()) {
            return;
        }

        ExposureFrame frame = photograph.getItem().getFrame(photograph.getItemStack());
        if (frame == ExposureFrame.EMPTY) {
            return;
        }

        guiGraphics.drawString(font, "?", width - font.width("?") - 10, 10, 0xFFFFFFFF);

        if (mouseX > width - 20 && mouseX < width && mouseY < 20) {
            String exposureName = frame.identifier().map(id -> id, ResourceLocation::toString);

            List<Component> lines = List.of(
                    Component.literal(exposureName),
                    Component.translatable("gui.exposure.photograph_screen.drop_as_item_tooltip", Component.literal("CTRL + I")),
                    Component.translatable("gui.exposure.photograph_screen.copy_" +
                            frame.identifier().map(id -> "id", texture -> "texture_path") + "_tooltip", "CTRL + C"));

            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY + 20);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemAndStack<PhotographItem> photograph = photographs.get(pager.getCurrentPage());

        ExposureFrame frame = photograph.getItem().getFrame(photograph.getItemStack());

        if (Screen.hasControlDown() && player != null && player.isCreative() && frame != ExposureFrame.EMPTY) {
            if (keyCode == InputConstants.KEY_C) {
                String text = frame.identifier().map(id -> id, ResourceLocation::toString);
                Minecraft.getInstance().keyboardHandler.setClipboard(text);
                player.displayClientMessage(Component.translatable("gui.exposure.photograph_screen.copied_message", text), false);
                return true;
            }

            if (keyCode == InputConstants.KEY_I) {
                if (Minecraft.getInstance().gameMode != null) {
                    Minecraft.getInstance().gameMode.handleCreativeModeItemDrop(photograph.getItemStack().copy());
                    player.displayClientMessage(Component.translatable("gui.exposure.photograph_screen.item_dropped_message",
                            photograph.getItemStack().toString()), false);
                }
                return true;
            }
        }

        return pager.handleKeyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return pager.handleKeyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void trySaveToFile(ItemAndStack<PhotographItem> photograph) {
        LocalPlayer player = Minecraft.getInstance().player;
        ExposureFrame frame = photograph.getItem().getFrame(photograph.getItemStack());

        if (player == null || frame == ExposureFrame.EMPTY || !frame.isTakenBy(player)) {
            return;
        }

        frame.identifier().ifId(id -> {
            if (StringUtil.isBlank(id)) {
                return;
            }

            PhotographRenderProperties properties = PhotographRenderProperties.get(photograph.getItemStack());
            String filename = properties != PhotographRenderProperties.DEFAULT ? id + "_" + properties.getId() : id;

            if (savedExposures.contains(filename))
                return;

            ExposureClient.exposureCache().getOrQuery(id).ifPresent(exposure -> {
                savedExposures.add(filename);

                new Thread(() -> new ClientsideExposureExporter(filename)
                        .withDefaultFolder()
                        .organizeByWorld(Config.Client.EXPOSURE_SAVING_LEVEL_SUBFOLDER.get(), ClientsideWorldNameGetter::getWorldName)
                        .withModifier(properties.getModifier())
                        .withSize(Config.Client.EXPOSURE_SAVING_SIZE.get())
                        .export(exposure), "ExposureSaving").start();
            });
        });
    }
}
