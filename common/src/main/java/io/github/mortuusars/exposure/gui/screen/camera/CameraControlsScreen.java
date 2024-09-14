package io.github.mortuusars.exposure.gui.screen.camera;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.core.CameraAccessors;
import io.github.mortuusars.exposure.core.NewCamera;
import io.github.mortuusars.exposure.core.camera.AttachmentType;
import io.github.mortuusars.exposure.core.camera.ZoomDirection;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderOverlay;
import io.github.mortuusars.exposure.client.MouseHandler;
import io.github.mortuusars.exposure.gui.screen.camera.button.*;
import io.github.mortuusars.exposure.item.FilmRollItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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

    public static final WidgetSprites SEPARATOR_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/button_separator"),
            Exposure.resource("camera_controls/button_separator"));
    public static final int SEPARATOR_WIDTH = 1;

    private final Player player;
    private final ClientLevel level;
    private final long openedAtTimestamp;
    private final NewCamera camera;

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

        int leftPos = (width - 256) / 2;
        int topPos = Math.round(ViewfinderOverlay.opening.y + ViewfinderOverlay.opening.height - 256);

        boolean hasFlash = !camera.getItem().getAttachment(camera.getItemStack(), AttachmentType.FLASH).isEmpty();

        int sideButtonsWidth = 48;
        int buttonWidth = 15;

        int elementX = leftPos + 128 - (sideButtonsWidth + 1 + buttonWidth + 1 + (hasFlash ? buttonWidth + 1 : 0) + sideButtonsWidth) / 2;
        int elementY = topPos + 238;

        // Order of adding influences TAB key behavior

        ShutterSpeedButton shutterSpeedButton = new ShutterSpeedButton(leftPos + 94, topPos + 226, 69, 12, SHUTTER_SPEED_SPRITES);
        addRenderableWidget(shutterSpeedButton);

        FocalLengthButton focalLengthButton = new FocalLengthButton(elementX, elementY, 48, 18, FOCAL_LENGTH_SPRITES);
        addRenderableOnly(focalLengthButton);
        elementX += focalLengthButton.getWidth();

        addSeparator(elementX, elementY);
        elementX += SEPARATOR_WIDTH;

        CompositionGuideButton compositionGuideButton = new CompositionGuideButton(elementX, elementY, 15, 18);
        addRenderableWidget(compositionGuideButton);
        elementX += compositionGuideButton.getWidth();

        addSeparator(elementX, elementY);
        elementX += SEPARATOR_WIDTH;

        if (hasFlash) {
            FlashModeButton flashModeButton = new FlashModeButton(elementX, elementY, 15, 18);
            addRenderableWidget(flashModeButton);
            elementX += flashModeButton.getWidth();

            addSeparator(elementX, elementY);
            elementX += SEPARATOR_WIDTH;
        }

        FrameCounterButton frameCounterButton = new FrameCounterButton(elementX, elementY, 48, 18, FRAME_COUNTER_SPRITES);
        addRenderableOnly(frameCounterButton);
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
        addRenderableOnly(new ImageButton(x, y, 1, 18, SEPARATOR_SPRITES, pButton -> {}));
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
