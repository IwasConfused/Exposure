package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.FocalLengthButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.FrameCounterButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.ShutterSpeedButton;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.input.MouseHandler;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraSettings;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuides;
import io.github.mortuusars.exposure.world.camera.component.FlashMode;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.item.part.Attachment;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ActiveCameraReleaseC2SP;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ViewfinderCameraControlsScreen extends Screen {
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

    public static final int SEPARATOR_WIDTH = 1;
    private static final int BUTTON_HEIGHT = 18;
    private static final int SIDE_BUTTONS_WIDTH = 48;
    private static final int BUTTON_WIDTH = 15;

    protected final Camera camera;
    protected final Viewfinder viewfinder;
    private final long openedAt;

    private int leftPos;
    private int topPos;

    public ViewfinderCameraControlsScreen(Camera camera, Viewfinder viewfinder) {
        super(CommonComponents.EMPTY);
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.openedAt = Minecrft.level().getGameTime();
    }

    public Camera getCamera() {
        return camera;
    }

    public Viewfinder getViewfinder() {
        return viewfinder;
    }

    // --

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
        topPos = Math.round(viewfinder.overlay().getOpening().y + viewfinder.overlay().getOpening().height - 256);

        boolean hasFlash = Attachment.FLASH.isPresent(camera.getItemStack());

        int elementX = leftPos + 128 - (SIDE_BUTTONS_WIDTH + 1 + BUTTON_WIDTH + 1 + (hasFlash ? BUTTON_WIDTH + 1 : 0) + SIDE_BUTTONS_WIDTH) / 2;
        int elementY = topPos + 238;

        // Order of adding influences TAB key behavior

        Button shutterSpeedButton = createShutterSpeedButton();
        addRenderableWidget(shutterSpeedButton);

        FocalLengthButton focalLengthButton = new FocalLengthButton(elementX, elementY, SIDE_BUTTONS_WIDTH, BUTTON_HEIGHT, FOCAL_LENGTH_SPRITES);
        focalLengthButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.focal_length.tooltip")));
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
        List<ShutterSpeed> shutterSpeeds = camera.map((item, stack) -> item.getAvailableShutterSpeeds(), List.of(ShutterSpeed.DEFAULT));
        ShutterSpeed currentShutterSpeed = camera.map(CameraSettings.SHUTTER_SPEED::getOrDefault, ShutterSpeed.DEFAULT);

        return new ShutterSpeedButton(leftPos + 94, topPos + 226, 69, 12, shutterSpeeds,
                currentShutterSpeed, speed -> SHUTTER_SPEED_SPRITES, (b, speed) -> camera.setSettingAndSync(CameraSettings.SHUTTER_SPEED, speed))
                .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.shutter_speed.tooltip")))
                .setClickSound(Exposure.SoundEvents.CAMERA_DIAL_CLICK.get());
    }

    protected @NotNull Button createCompositionGuideButton() {
        List<CompositionGuide> guides = CompositionGuides.getGuides();
        CompositionGuide currentGuide = camera.map(CameraSettings.COMPOSITION_GUIDE::getOrDefault, CompositionGuides.NONE);
        Function<CompositionGuide, WidgetSprites> spritesFunc = guide -> Widgets.threeStateSprites(guide.buttonSpriteLocation());

        return new CycleButton<>(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, guides,
                currentGuide, spritesFunc, (b, guide) -> camera.setSettingAndSync(CameraSettings.COMPOSITION_GUIDE, guide))
                .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.composition_guide.tooltip")))
                .setTooltips(guide -> Component.translatable("gui.exposure.camera_controls.composition_guide.tooltip")
                        .append(CommonComponents.NEW_LINE)
                        .append(guide.translate().withStyle(ChatFormatting.GRAY)))
                .setClickSound(Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get());
    }

    protected @NotNull Button createFlashModeButton() {
        List<FlashMode> modes = Arrays.asList(FlashMode.values());
        FlashMode currentMode = camera.map(CameraSettings.FLASH_MODE::getOrDefault, FlashMode.OFF);
        Function<FlashMode, WidgetSprites> spritesFunc = mode -> Widgets.threeStateSprites(
                Exposure.resource("camera_controls/flash_mode/flash_" + mode.getSerializedName()));

        return new CycleButton<>(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, modes,
                currentMode, spritesFunc, (b, mode) -> camera.setSettingAndSync(CameraSettings.FLASH_MODE, mode))
                .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.flash_mode.tooltip")))
                .setTooltips(mode -> Component.translatable("gui.exposure.camera_controls.flash_mode.tooltip")
                        .append(CommonComponents.NEW_LINE)
                        .append(mode.translate().withStyle(ChatFormatting.GRAY)))
                .setClickSound(Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get());
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

        update.accept(KeyboardHandler.getCameraControlsKey());
        Options opt = Minecrft.options();
        update.accept(opt.keyUp);
        update.accept(opt.keyDown);
        update.accept(opt.keyLeft);
        update.accept(opt.keyRight);
        update.accept(opt.keyJump);
        update.accept(opt.keySprint);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!viewfinder.isLookingThrough()) {
            this.onClose();
            return;
        }

        if (Minecrft.options().hideGui) return;

        guiGraphics.pose().pushPose();

        float viewfinderScale = viewfinder.overlay().getScale();
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
        // Prevents blur from rendering.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            if (camera.isActive()) {
                camera.release();
                Packets.sendToServer(ActiveCameraReleaseC2SP.INSTANCE);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (KeyboardHandler.getCameraControlsKey().matchesMouse(button)
                || (Config.Client.VIEWFINDER_MIDDLE_CLICK_CONTROLS.get() && button == InputConstants.MOUSE_BUTTON_MIDDLE)) {
            if (isToggleTimeReached())
                this.onClose();

            return false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (KeyboardHandler.getCameraControlsKey().matches(keyCode, scanCode)) {
            if (isToggleTimeReached())
                this.onClose();

            return false;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    protected boolean isToggleTimeReached() {
        return Minecrft.level().getGameTime() - openedAt >= 5;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);

        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS) {
            viewfinder.zoom().zoom(ZoomDirection.IN, true);
            return true;
        } else if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS) {
            viewfinder.zoom().zoom(ZoomDirection.OUT, true);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            viewfinder.zoom().zoom(scrollY > 0d ? ZoomDirection.IN : ZoomDirection.OUT, true);
            return true;
        }

        return false;
    }
}
