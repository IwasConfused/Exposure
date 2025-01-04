package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.item.FilmRollItem;
import io.github.mortuusars.exposure.item.part.Attachment;
import io.github.mortuusars.exposure.item.part.CameraSetting;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.util.Rect2f;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ViewfinderOverlay {
    public static final ResourceLocation VIEWFINDER_TEXTURE = Exposure.resource("textures/gui/viewfinder/viewfinder.png");
    public static final ResourceLocation NO_FILM_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/no_film.png");
    public static final ResourceLocation REMAINING_FRAMES_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/remaining_frames.png");

    protected final LocalPlayer player;
    protected final Camera camera;
    protected final Viewfinder viewfinder;
    protected final int backgroundColor;
    protected final Rect2f opening;

    protected final Animation scaleAnimation;
    protected final float initialScale;

    protected float scale;
    protected float bobX = 0f;
    protected float bobY = 0f;
    protected float attackAnim = 0f;
    protected float xRot;
    protected float yRot;
    protected float xRot0;
    protected float yRot0;

    public ViewfinderOverlay(Camera camera, Viewfinder viewfinder) {
        this.player = Minecrft.player();
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.backgroundColor = Config.Client.getBackgroundColor();
        this.opening = new Rect2f(0, 0, 0, 0);
        recalculateOpening();

        this.scaleAnimation = new Animation(300, EasingFunction.EASE_OUT_EXPO);
        this.initialScale = 0.5f;
        this.scale = 0.5f; // Start small to animate expanding

        this.xRot = Minecrft.get().gameRenderer.getMainCamera().getXRot();
        this.yRot = Minecrft.get().gameRenderer.getMainCamera().getYRot();
        this.xRot0 = xRot;
        this.yRot0 = yRot;
    }

    public Rect2f getOpening() {
        return opening;
    }

    public float getScale() {
        return scale;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        recalculateOpening();
        scale = Mth.lerp((float)scaleAnimation.getValue(), initialScale, 1f);

        // opening and scale is updated even if overlay is not rendered - other classes may depend on them.

        if (!viewfinder.isLookingThrough() || Minecrft.options().hideGui || camera.isEmpty()) return;

        final int width = Minecrft.get().getWindow().getGuiScaledWidth();
        final int height = Minecrft.get().getWindow().getGuiScaledHeight();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(width / 2f, height / 2f, 0);
        guiGraphics.pose().scale(scale, scale, scale);

        if (Minecrft.options().bobView().get())
            bobView(guiGraphics.pose(), deltaTracker);
        applyAttackAnimation(guiGraphics.pose(), deltaTracker);
        applyMovementDelay(guiGraphics.pose(), deltaTracker);

        guiGraphics.pose().translate(-width / 2f, -height / 2f, 0);

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // -9999 to cover all screen when overlay is scaled down
        // Left
        GuiUtil.drawRect(guiGraphics, opening.x, opening.y, -9999, opening.height, backgroundColor);
        // Right
        GuiUtil.drawRect(guiGraphics, opening.x + opening.width, opening.y, 9999, opening.height, backgroundColor);
        // Top
        GuiUtil.drawRect(guiGraphics, -4999, opening.y, 9999, -9999, backgroundColor);
        // Bottom
        GuiUtil.drawRect(guiGraphics, -4999, opening.y + opening.height, 9999, 9999, backgroundColor);

        // Shutter
        if (camera.isShutterOpen()) {
            GuiUtil.drawRect(guiGraphics, opening, 0xfa1f1d1b);
        }

        // Opening Texture
        GuiUtil.blit(VIEWFINDER_TEXTURE, guiGraphics.pose(), opening, 0, 0, (int)opening.width, (int)opening.height, 0);

        // Guide
        ResourceLocation guideTexture = CameraSetting.COMPOSITION_GUIDE.getOrDefault(camera.getItemStack()).overlayTextureLocation();
        GuiUtil.blit(guideTexture, guiGraphics.pose(), opening, 0, 0, (int)opening.width, (int)opening.height, 0);

        if (!(Minecrft.get().screen instanceof ViewfinderCameraControlsScreen)) {
            renderStatusIcons(guiGraphics.pose(), camera.getItemStack());
        }

        guiGraphics.pose().popPose();
    }

    public void bobView(PoseStack poseStack, DeltaTracker deltaTracker) {
        if (Minecrft.get().getCameraEntity() instanceof Player pl) {
            float walkDist = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), pl.walkDistO, pl.walkDist);
            float strength = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), pl.oBob, pl.bob);
            float x = Mth.sin(walkDist * (float) Math.PI) * strength;
            float y = Math.abs(Mth.cos(walkDist * (float) Math.PI) * strength);

            float delta = Math.min(deltaTracker.getGameTimeDeltaTicks(), 1f);
            bobX = Mth.lerp(delta, bobX, x);
            bobY = Mth.lerp(delta, bobY, y);
            int guiScale = Minecrft.options().guiScale().get();
            poseStack.translate(bobX * 100 / guiScale, bobY * 200 / guiScale, 0.0F);
            float scale = this.scale - (bobY * 0.25f);
            poseStack.scale(scale, scale, scale);
        }
    }

    public void applyAttackAnimation(PoseStack poseStack, DeltaTracker deltaTracker) {
        float attack = player.attackAnim;
        if (attack > 0.1f)
            attack = 1f - attack;

        float delta = Math.min(deltaTracker.getGameTimeDeltaTicks(), 1f);
        attackAnim = Mth.lerp(delta, attackAnim, attack);

        poseStack.scale(1f - attackAnim * 0.1f, 1f - attackAnim * 0.2f, 1f - attackAnim * 0.1f);
        poseStack.mulPose(Axis.ZN.rotationDegrees(Mth.lerp(attackAnim, 0, 5)));
        poseStack.translate(0, 60f / Minecrft.options().guiScale().get() * attackAnim, 0);
    }

    public void applyMovementDelay(PoseStack poseStack, DeltaTracker deltaTracker) {
        float delta = Math.min(deltaTracker.getGameTimeDeltaTicks() * 0.6f, 1.0f);
        xRot0 = Mth.lerp(delta, xRot0, xRot);
        yRot0 = Mth.lerp(delta, yRot0, yRot);
        xRot = Minecrft.get().gameRenderer.getMainCamera().getXRot();
        yRot = Minecrft.get().gameRenderer.getMainCamera().getYRot();
        int guiScale = Minecrft.options().guiScale().get();
        float horizontalDelay = (yRot - yRot0) / guiScale * 3;
        float verticalDelay = (xRot - xRot0) / guiScale * 3;
        poseStack.translate(-horizontalDelay, -verticalDelay, 0);
    }

    protected void recalculateOpening() {
        final int width = Minecrft.get().getWindow().getGuiScaledWidth();
        final int height = Minecrft.get().getWindow().getGuiScaledHeight();
        final float openingSize = Math.min(width, height);

        opening.x = (width - openingSize) / 2f;
        opening.y = (height - openingSize) / 2f;
        opening.width = openingSize;
        opening.height = openingSize;
    }

    protected void renderStatusIcons(PoseStack poseStack, ItemStack cameraStack) {
        ItemStack filmStack = Attachment.FILM.get(cameraStack).getForReading();

        if (filmStack.isEmpty()
                || !(filmStack.getItem() instanceof FilmRollItem filmRollItem)
                || !filmRollItem.canAddFrame(filmStack)) {
            renderNoFilmIcon(poseStack);
            return;
        }

        renderRemainingFramesIcon(poseStack, filmRollItem, filmStack);
    }

    protected void renderNoFilmIcon(PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, NO_FILM_ICON_TEXTURE);
        int x = (int) ((opening.x + (opening.width / 2) - 12));
        int y = (int) (opening.y + opening.height - 18);
        GuiUtil.blit(poseStack, x, y, 23, 18, 0, 0, 23, 18, 0);
    }

    protected void renderRemainingFramesIcon(PoseStack poseStack, FilmRollItem filmRollItem, ItemStack filmStack) {
        int maxFrames = filmRollItem.getMaxFrameCount(filmStack);
        int exposedFrames = filmRollItem.getStoredFramesCount(filmStack);
        int remainingFrames = Math.max(0, maxFrames - exposedFrames);
        if (maxFrames > 5 && remainingFrames <= 3) {
            RenderSystem.setShaderTexture(0, REMAINING_FRAMES_ICON_TEXTURE);
            float x = (int)(opening.x + (opening.width / 2) - 17);
            float y = (int)(opening.y + opening.height - 15);
            int vOffset = (remainingFrames - 1) * 15;
            GuiUtil.blit(poseStack, x, y, 33, 15, 0, vOffset, 33, 45, 0);
        }
    }
}
