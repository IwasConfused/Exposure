package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.input.Modifier;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PhotographScreen extends Screen {
    protected final Pager pager = new Pager()
            .setCycled(true)
            .setChangeSound(new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK))
            .onPageChanged(this::pageChanged);

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyInventory).executes(this::onClose),
            Key.press(InputConstants.KEY_ADD).or(Key.press(InputConstants.KEY_EQUALS)).executes(this::zoomIn),
            Key.press(GLFW.GLFW_KEY_KP_SUBTRACT).or(Key.press(InputConstants.KEY_MINUS)).executes(this::zoomOut),
            Key.press(Modifier.CONTROL, InputConstants.KEY_C).executes(this::copyIdentifierToClipboard),
            Key.press(Modifier.CONTROL, InputConstants.KEY_I).executes(this::dropAsItem),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::previousPage),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::nextPage),
            Key.release(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::resetCooldown),
            Key.release(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::resetCooldown)
    );

    protected final ZoomAnimationController zoom = new ZoomAnimationController();
    protected float zoomFactor = 1.0f;
    protected float scale = 1.0f;
    protected float x;
    protected float y;

    protected final List<String> savedExposures = new ArrayList<>();

    protected List<ItemAndStack<PhotographItem>> photographs = new ArrayList<>();

    public PhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        super(Component.empty());
        Preconditions.checkState(!photographs.isEmpty(), "No photographs to display.");
        this.photographs.addAll(photographs);
        this.pager.setPagesCount(photographs.size());

        if (shouldQueryAllPhotographsImmediately()) {
            queryAllPhotographs(photographs);
        }
    }

    @Override
    protected void init() {
        super.init();
        zoomFactor = (float) height;

        ImageButton previousButton = new ImageButton(0, (int) (height / 2f - 16 / 2f), 16, 16,
                Widgets.PREVIOUS_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        addRenderableWidget(previousButton);

        ImageButton nextButton = new ImageButton(width - 16, (int) (height / 2f - 16 / 2f), 16, 16,
                Widgets.NEXT_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        addRenderableWidget(nextButton);

        pager.setPagesCount(photographs.size())
                .setPreviousPageButton(previousButton)
                .setNextPageButton(nextButton);
    }

    protected boolean shouldQueryAllPhotographsImmediately() {
        return true;
    }

    protected void queryAllPhotographs(List<ItemAndStack<PhotographItem>> photographs) {
        for (ItemAndStack<PhotographItem> photograph : photographs) {
            photograph.getItem().getFrame(photograph.getItemStack())
                    .exposureIdentifier()
                    .ifId(id -> ExposureClient.exposureStore().getOrRequest(id));
        }
    }

    public ItemAndStack<PhotographItem> getCurrentPhotograph() {
        return photographs.getFirst();
    }

    protected void pageChanged(int oldPage, int newPage) {
        int distance = newPage - oldPage;
        Collections.rotate(photographs, -distance);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        zoomFactor = 0.8f;
        zoom.update();
        scale = zoom.get() * zoomFactor * 256;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        guiGraphics.pose().pushPose();
        renderBlurredBackground(partialTick);
        renderTransparentBackground(guiGraphics);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().translate(width / 2f, height / 2f, 50);
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.pose().translate(-0.5, -0.5, 0);

        MultiBufferSource.BufferSource bufferSource = Minecrft.get().renderBuffers().bufferSource();

        ArrayList<ItemAndStack<PhotographItem>> photos = new ArrayList<>(photographs);
        ExposureClient.photographRenderer().renderStackedPhotographs(photos, guiGraphics.pose(), bufferSource,
                LightTexture.FULL_BRIGHT, 255, 255, 255, 255);

        bufferSource.endBatch();
        guiGraphics.pose().popPose();

        ItemAndStack<PhotographItem> photograph = getCurrentPhotograph();

        guiGraphics.pose().pushPose();
        // Places widgets above photograph, because they will be covered when photo is zoomed in
        guiGraphics.pose().translate(0, 0, 100);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderFrameInfoHint(guiGraphics, mouseX, mouseY, photograph);
        guiGraphics.pose().popPose();

        if (Config.Client.SAVE_EXPOSURE_TO_FILE_WHEN_VIEWED.get())
            trySaveToFile(photograph);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Background is rendered manually in #render method.
        // Otherwise, background will be rendered on top of a photograph.
    }

    private void renderFrameInfoHint(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, ItemAndStack<PhotographItem> photograph) {
        if (Minecrft.get().player == null || !Minecrft.get().player.isCreative()) {
            return;
        }

        Frame frame = photograph.getItem().getFrame(photograph.getItemStack());
        if (frame == Frame.EMPTY) {
            return;
        }

        guiGraphics.drawString(font, "?", width - font.width("?") - 10, 10, 0xFFFFFFFF);

        if (mouseX > width - 20 && mouseX < width && mouseY < 20) {
            String exposureName = frame.exposureIdentifier().map(id -> id, ResourceLocation::toString);

            List<Component> lines = List.of(
                    Component.literal(exposureName),
                    Component.translatable("gui.exposure.photograph_screen.drop_as_item_tooltip", Component.literal("CTRL + I")),
                    Component.translatable("gui.exposure.photograph_screen.copy_" +
                            frame.exposureIdentifier().map(id -> "id", texture -> "texture_path") + "_tooltip", "CTRL + C"));

            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY + 20);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;

        if (scrollY >= 0.0) {
            zoomIn();
        } else {
            zoomOut();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            float centerX = width / 2f;
            float centerY = height / 2f;
            x = (float) Mth.clamp(x + dragX, -centerX, centerX);
            y = (float) Mth.clamp(y + dragY, -centerY, centerY);
            return true;
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --

    protected void zoomIn() {
        zoom.change(ZoomDirection.IN);
    }

    protected void zoomOut() {
        zoom.change(ZoomDirection.OUT);
    }

    protected boolean copyIdentifierToClipboard() {
        Frame frame = getCurrentPhotograph().map(PhotographItem::getFrame);
        if (!Minecrft.player().isCreative() || frame.equals(Frame.EMPTY)) {
            return false;
        }
        String text = frame.exposureIdentifier().map(id -> id, ResourceLocation::toString);
        Minecrft.get().keyboardHandler.setClipboard(text);
        Minecrft.player().displayClientMessage(
                Component.translatable("gui.exposure.photograph_screen.copied_message", text), false);
        return true;
    }

    protected boolean dropAsItem() {
        ItemStack droppedStack = getCurrentPhotograph().getItemStack().copy();
        Minecrft.gameMode().handleCreativeModeItemDrop(droppedStack);
        Minecrft.player().displayClientMessage(Component.translatable("gui.exposure.photograph_screen.item_dropped_message",
                droppedStack.getDisplayName()), false);
        return true;
    }

    // --

    protected void trySaveToFile(ItemAndStack<PhotographItem> photograph) {
        LocalPlayer player = Minecrft.get().player;
        Frame frame = photograph.getItem().getFrame(photograph.getItemStack());

        if (player == null || frame == Frame.EMPTY || !frame.isTakenBy(player)) {
            return;
        }

        frame.exposureIdentifier().ifId(id -> {
            PhotographType photographType = photograph.getItem().getType(photograph.getItemStack());
            PhotographStyle photographStyle = PhotographStyle.of(photograph.getItemStack());

            String filename = getFilename(id, photographType);

            if (savedExposures.contains(filename))
                return;

            ExposureData exposureData = ExposureClient.exposureStore().getOrRequest(id).orElse(ExposureData.EMPTY);
            if (!exposureData.equals(ExposureData.EMPTY)) {
                savedExposures.add(filename);

                Exposure.LOGGER.error("Saving not implemented yet!");

//                CompletableFuture.runAsync(() -> new ClientsideExposureExporter(filename)
//                        .withDefaultFolder()
//                        .organizeByWorld(Config.Client.EXPOSURE_SAVING_LEVEL_SUBFOLDER.get(), LevelNameGetter::getWorldName)
//                        .withModifier(photographFeatures.pixelModifier())
//                        .withSize(Config.Client.EXPOSURE_SAVING_SIZE.get())
//                        .export(palettedExposure));
            }
        });
    }

    protected @NotNull String getFilename(String id, PhotographType photographType) {
        String suffix = photographType.getFileSuffix();
        if (!StringUtil.isNullOrEmpty(suffix)) {
            return id + "_" + suffix;
        }
        return id;
    }
}
