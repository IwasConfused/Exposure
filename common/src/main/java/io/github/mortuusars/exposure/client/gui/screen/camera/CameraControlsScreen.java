package io.github.mortuusars.exposure.client.gui.screen.camera;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.*;
import io.github.mortuusars.exposure.core.Camera;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.core.CameraAccessors;
import io.github.mortuusars.exposure.core.camera.*;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderOverlay;
import io.github.mortuusars.exposure.client.input.MouseHandler;
import io.github.mortuusars.exposure.item.FilmRollItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CameraControlsScreen extends Screen {
    public static final WidgetSprites SHUTTER_SPEED_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/shutter_speed_dial"),
            Exposure.resource("camera_controls/shutter_speed_dial_disabled"),
            Exposure.resource("camera_controls/shutter_speed_dial_highlighted"));

    public static final WidgetSprites FOCAL_LENGTH_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/focal_length"),
            Exposure.resource("camera_controls/focal_length_disabled"),
            Exposure.resource("camera_controls/focal_length_highlighted"));

    public static final WidgetSprites FRAME_COUNTER_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/frame_counter"),
            Exposure.resource("camera_controls/frame_counter_disabled"),
            Exposure.resource("camera_controls/frame_counter_highlighted"));

    public static final ResourceLocation SEPARATOR_SPRITE = Exposure.resource("camera_controls/button_separator");

    public static final WidgetSprites SEPARATOR_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/button_separator"),
            Exposure.resource("camera_controls/button_separator"));
    public static final int SEPARATOR_WIDTH = 1;
    private static final int BUTTON_HEIGHT = 18;
    private static final int SIDE_BUTTONS_WIDTH = 48;
    private static final int BUTTON_WIDTH = 15;

    private final Player player;
    private final ClientLevel level;
    private final long openedAtTimestamp;
    private final Camera camera;
    private int leftPos;
    private int topPos;

    public CameraControlsScreen() {
        super(Component.empty());

        camera = CameraClient.getActiveCamera().orElseThrow();

        player = Minecraft.getInstance().player;
        level = Minecraft.getInstance().level;
        assert level != null;
        openedAtTimestamp = level.getGameTime();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        refreshMovementKeys();
        Minecraft.getInstance().handleKeybinds();
    }

    @Override
    protected void init() {
        super.init();
        refreshMovementKeys();

        leftPos = (width - 256) / 2;
        topPos = Math.round(ViewfinderOverlay.opening.y + ViewfinderOverlay.opening.height - 256);

        boolean hasFlash = camera.getItem().hasFlash(camera.getItemStack());

        int elementX = leftPos + 128 - (SIDE_BUTTONS_WIDTH + 1 + BUTTON_WIDTH + 1 + (hasFlash ? BUTTON_WIDTH + 1 : 0) + SIDE_BUTTONS_WIDTH) / 2;
        int elementY = topPos + 238;

        // Order of adding influences TAB key behavior

        Button shutterSpeedButton = createShutterSpeedButton();
        addRenderableWidget(shutterSpeedButton);

        FocalLengthButton focalLengthButton = new FocalLengthButton(elementX, elementY, SIDE_BUTTONS_WIDTH, BUTTON_HEIGHT, FOCAL_LENGTH_SPRITES);
        addRenderableOnly(focalLengthButton);
        elementX += focalLengthButton.getWidth();

        addSeparator(elementX, elementY);
        elementX += SEPARATOR_WIDTH;

        Button compositionGuideButton = createCompositionGuideButton();
        compositionGuideButton.setX(elementX);
        compositionGuideButton.setY(elementY);
        addRenderableWidget(compositionGuideButton);
        elementX += compositionGuideButton.getWidth();

        addSeparator(elementX, elementY);
        elementX += SEPARATOR_WIDTH;

        if (hasFlash) {
            Button flashModeButton = createFlashModeButton();
            flashModeButton.setX(elementX);
            flashModeButton.setY(elementY);
            addRenderableWidget(flashModeButton);
            elementX += flashModeButton.getWidth();

            addSeparator(elementX, elementY);
            elementX += SEPARATOR_WIDTH;
        }

        FrameCounterButton frameCounterButton = new FrameCounterButton(elementX, elementY, SIDE_BUTTONS_WIDTH, BUTTON_HEIGHT, FRAME_COUNTER_SPRITES);
        addRenderableOnly(frameCounterButton);
    }

    protected @NotNull Button createShutterSpeedButton() {
        List<ShutterSpeed> shutterSpeeds = CameraClient.getActiveCamera()
                .map(camera -> camera.getItem().getShutterSpeeds(camera.getItemStack()))
                .orElse(List.of(ShutterSpeed.DEFAULT));
        ShutterSpeed currentShutterSpeed = CameraClient.getActiveCamera()
                .map(camera -> camera.getItem().getShutterSpeed(camera.getItemStack()))
                .orElse(ShutterSpeed.DEFAULT);

        return new ShutterSpeedButton(leftPos + 94, topPos + 226, 69, 12, shutterSpeeds,
                currentShutterSpeed, speed -> SHUTTER_SPEED_SPRITES, (b, speed) -> CameraClient.setShutterSpeed(speed))
                .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.shutter_speed.tooltip")))
                .setTooltips(Collections.emptyMap())
                .setClickSound(Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get());
    }

    protected @NotNull Button createCompositionGuideButton() {
        List<CompositionGuide> guides = CompositionGuides.getGuides();
        CompositionGuide currentGuide = CameraClient.getActiveCamera()
                .map(camera -> camera.getItem().getCompositionGuide(camera.getItemStack()))
                .orElse(CompositionGuides.NONE);
        Function<CompositionGuide, WidgetSprites> spritesFunc = guide -> Widgets.threeStateSprites(
                Exposure.resource("camera_controls/composition_guide/" + guide.name()));

        return new CycleButton<>(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, guides,
                currentGuide, spritesFunc, (b, guide) -> CameraClient.setCompositionGuide(guide))
                .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.composition_guide.tooltip")))
                .setTooltips(guide -> Component.translatable("gui.exposure.camera_controls.composition_guide.tooltip")
                        .append(CommonComponents.NEW_LINE)
                        .append(guide.translate().withStyle(ChatFormatting.GRAY)))
                .setClickSound(Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get());
    }

    protected @NotNull Button createFlashModeButton() {
        List<FlashMode> modes = Arrays.asList(FlashMode.values());
        FlashMode currentMode = CameraClient.getActiveCamera()
                .map(camera -> camera.getItem().getFlashMode(camera.getItemStack()))
                .orElse(FlashMode.OFF);
        Function<FlashMode, WidgetSprites> spritesFunc = mode -> Widgets.threeStateSprites(
                Exposure.resource("camera_controls/flash_mode/flash_" + mode.getSerializedName()));

        return new CycleButton<>(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, modes,
                currentMode, spritesFunc, (b, mode) -> CameraClient.setFlashMode(mode))
                .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.flash_mode.tooltip")))
                .setTooltips(mode -> Component.translatable("gui.exposure.camera_controls.flash_mode.tooltip")
                        .append(CommonComponents.NEW_LINE)
                        .append(mode.translate().withStyle(ChatFormatting.GRAY)))
                .setClickSound(Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get());
    }

    protected boolean cameraHasAvailableFrames() {
        return CameraClient.getActiveCamera().map(camera -> {
            ItemStack filmStack = camera.getItem().getAttachment(camera.getItemStack(), AttachmentType.FILM).getForReading();
            if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmItem)) {
                return false;
            }
            return filmItem.canAddFrame(filmStack);
        }).orElse(false);
    }

    protected void addSeparator(int x, int y) {
        ImageWidget sprite = ImageWidget.sprite(SEPARATOR_WIDTH, BUTTON_HEIGHT, SEPARATOR_SPRITE);
        sprite.setX(x);
        sprite.setY(y);
        addRenderableOnly(sprite);
    }

    /**
     * When screen is opened - all keys are released. If we do not refresh them - player would stop moving (if they had).
     */
    protected void refreshMovementKeys() {
        Consumer<KeyMapping> update = keyMapping -> {
            if (keyMapping.key.getType() == InputConstants.Type.MOUSE) {
                keyMapping.setDown(MouseHandler.isMouseButtonHeld(keyMapping.key.getValue()));
            } else {
                long windowId = Minecraft.getInstance().getWindow().getWindow();
                keyMapping.setDown(InputConstants.isKeyDown(windowId, keyMapping.key.getValue()));
            }
        };

        update.accept(ExposureClient.getCameraControlsKey());
        Options opt = Minecraft.getInstance().options;
        update.accept(opt.keyUp);
        update.accept(opt.keyDown);
        update.accept(opt.keyLeft);
        update.accept(opt.keyRight);
        update.accept(opt.keyJump);
        update.accept(opt.keySprint);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!Viewfinder.isLookingThrough()) {
            this.onClose();
            return;
        }

        if (Minecraft.getInstance().options.hideGui)
            return;

        guiGraphics.pose().pushPose();

        float viewfinderScale = ViewfinderOverlay.getScale();
        if (viewfinderScale != 1.0f) {
            guiGraphics.pose().translate(width / 2f, height / 2f, 0);
            guiGraphics.pose().scale(viewfinderScale, viewfinderScale, viewfinderScale);
            guiGraphics.pose().translate(-width / 2f, -height / 2f, 0);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        if (button == InputConstants.MOUSE_BUTTON_RIGHT && Minecraft.getInstance().gameMode != null) {
            CameraAccessor cameraAccessor = CameraClient.getActiveCameraAccessor();

            if (cameraAccessor == CameraAccessors.MAIN_HAND || cameraAccessor == CameraAccessors.OFF_HAND) {
                Minecraft.getInstance().startUseItem();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (ExposureClient.getCameraControlsKey().matchesMouse(button)
                || (Config.Client.VIEWFINDER_MIDDLE_CLICK_CONTROLS.get() && button == InputConstants.MOUSE_BUTTON_MIDDLE)) {
            if (level.getGameTime() - openedAtTimestamp >= 5)
                this.onClose();

            return false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ExposureClient.getCameraControlsKey().matches(keyCode, scanCode)) {
            if (level.getGameTime() - openedAtTimestamp >= 5)
                this.onClose();

            return false;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);

        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS) {
            Viewfinder.zoom(ZoomDirection.IN, true);
            return true;
        } else if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS) {
            Viewfinder.zoom(ZoomDirection.OUT, true);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            Viewfinder.zoom(scrollY > 0d ? ZoomDirection.IN : ZoomDirection.OUT, true);
            return true;
        }

        return false;
    }
}
