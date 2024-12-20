package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraControlsScreen;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.core.camera.component.CompositionGuides;
import io.github.mortuusars.exposure.item.FilmRollItem;
import io.github.mortuusars.exposure.item.part.Attachment;
import io.github.mortuusars.exposure.item.part.Setting;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.util.Rect2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class ViewfinderOverlay {
    public static final ResourceLocation VIEWFINDER_TEXTURE = Exposure.resource("textures/gui/viewfinder/viewfinder.png");
    public static final ResourceLocation NO_FILM_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/no_film_icon.png");
    public static final ResourceLocation REMAINING_FRAMES_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/remaining_frames_icon.png");

    private static final PoseStack POSE_STACK = new PoseStack();

    private final LocalPlayer player;
    private final Camera camera;
    private final Viewfinder viewfinder;
    private final int backgroundColor;
    private final Rect2f opening;

    private final Animation scaleAnimation;
    private final float initialScale;
    private float scale;

    private float xRot;
    private float yRot;
    private float xRot0;
    private float yRot0;

    public ViewfinderOverlay(Camera camera, Viewfinder viewfinder) {
        this.player = Minecrft.player();
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.backgroundColor = Config.Client.getBackgroundColor();
        this.opening = new Rect2f(0, 0, 0, 0);

        this.scaleAnimation = new Animation(300, EasingFunction.EASE_OUT_EXPO);
        this.initialScale = 0.5f;
        this.scale = 0.5f; // Start small to animate expanding

        this.xRot = player.getXRot();
        this.yRot = player.getYRot();
        this.xRot0 = xRot;
        this.yRot0 = yRot;
    }

    public float getScale() {
        return scale;
    }

    public void render() {
        final int width = Minecrft.get().getWindow().getGuiScaledWidth();
        final int height = Minecrft.get().getWindow().getGuiScaledHeight();

        float partialTicks = Minecrft.get().getTimer().getGameTimeDeltaTicks();
        scale = Mth.lerp((float)scaleAnimation.getValue(), initialScale, 1f);
        float openingSize = Math.min(width, height);

        opening.x = (width - openingSize) / 2f;
        opening.y = (height - openingSize) / 2f;
        opening.width = openingSize;
        opening.height = openingSize;

        if (!viewfinder.isLookingThrough() || Minecrft.options().hideGui || camera.isEmpty()) return;

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        float delta = Math.min(0.7f * partialTicks, 0.8f);
        xRot0 = Mth.lerp(delta, xRot0, xRot);
        yRot0 = Mth.lerp(delta, yRot0, yRot);
        xRot = player.getXRot();
        yRot = player.getYRot();
        float xDelay = (xRot - xRot0);
        float yDelay = (yRot - yRot0);
        // Adjust for gui scale:
        xDelay *= width / (float) Minecraft.getInstance().getWindow().getWidth();
        yDelay *= height / (float) Minecraft.getInstance().getWindow().getHeight();
        xDelay *= 3.5f;
        yDelay *= 3.5f;

        PoseStack poseStack = POSE_STACK;
        poseStack.pushPose();
        poseStack.translate(width / 2f, height / 2f, 0);
        poseStack.scale(scale, scale, scale);

        float attackAnim = player.getAttackAnim(partialTicks);
        if (attackAnim > 0.5f)
            attackAnim = 1f - attackAnim;
        poseStack.scale(1f - attackAnim * 0.4f, 1f - attackAnim * 0.6f, 1f - attackAnim * 0.4f);
        poseStack.translate(width / 16f * attackAnim, width / 5f * attackAnim, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(attackAnim, 0, 10)));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(attackAnim, 0, 100)));

        poseStack.translate(-width / 2f - yDelay, -height / 2f - xDelay, 0);

        if (Minecrft.options().bobView().get())
            bobView(poseStack, partialTicks);

        // -9999 to cover all screen when poseStack is scaled down.
        // Left
        drawRect(poseStack, -9999, opening.y, opening.x, opening.y + opening.height, backgroundColor);
        // Right
        drawRect(poseStack, opening.x + opening.width, opening.y, width + 9999, opening.y + opening.height, backgroundColor);
        // Top
        drawRect(poseStack, -9999, -9999, width + 9999, opening.y, backgroundColor);
        // Bottom
        drawRect(poseStack, -9999, opening.y + opening.height, width + 9999, height + 9999, backgroundColor);

        // Shutter
        if (camera.isShutterOpen()) {
            drawRect(poseStack, opening.x, opening.y, opening.x + opening.width, opening.y + opening.height, 0xfa1f1d1b);
        }

        // Opening Texture
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, VIEWFINDER_TEXTURE);
        GuiUtil.blit(poseStack, opening.x, opening.x + opening.width, opening.y, opening.y + opening.height, 0f, 0f, 1f, 0f, 1f);

        // Guide
        RenderSystem.setShaderTexture(0, Setting.COMPOSITION_GUIDE.getOrDefault(camera.getItemStack(), CompositionGuides.NONE).overlayTextureLocation());
        GuiUtil.blit(poseStack, opening.x, opening.x + opening.width, opening.y, opening.y + opening.height, -1f, 0f, 1f, 0f, 1f);

        if (!(Minecrft.get().screen instanceof CameraControlsScreen)) {
            //TODO: don't move with opening. render fixed in place
            renderStatusIcons(poseStack, camera.getItemStack());
        }

        poseStack.popPose();
    }

    private void renderStatusIcons(PoseStack poseStack, ItemStack cameraStack) {
        ItemStack filmStack = Attachment.FILM.get(cameraStack).getForReading();

        if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmRollItem) || !filmRollItem.canAddFrame(filmStack)) {
            renderNoFilmIcon(poseStack);
            return;
        }

        renderRemainingFramesIcon(poseStack, filmRollItem, filmStack);
    }

    private void renderNoFilmIcon(PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, NO_FILM_ICON_TEXTURE);
        GuiUtil.blit(poseStack, (opening.x + (opening.width / 2) - 12), opening.y + opening.height - 19,
                24, 19, 0, 0, 24, 19, 0);
    }

    private void renderRemainingFramesIcon(PoseStack poseStack, FilmRollItem filmRollItem, ItemStack filmStack) {
        int maxFrames = filmRollItem.getMaxFrameCount(filmStack);
        int exposedFrames = filmRollItem.getStoredFramesCount(filmStack);
        int remainingFrames = Math.max(0, maxFrames - exposedFrames);
        if (maxFrames > 5 && remainingFrames <= 3) {
            RenderSystem.setShaderTexture(0, REMAINING_FRAMES_ICON_TEXTURE);
            GuiUtil.blit(poseStack, (opening.x + (opening.width / 2) - 17), opening.y + opening.height - 15,
                    33, 15, 0, (remainingFrames - 1) * 15, 33, 45, 0);
        }
    }

    public void drawRect(PoseStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
        if (minX < maxX) {
            float temp = minX;
            minX = maxX;
            maxX = temp;
        }

        if (minY < maxY) {
            float temp = minY;
            minY = maxY;
            maxY = temp;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(matrix, minX, maxY, 0).setColor(color);
        bufferBuilder.addVertex(matrix, maxX, maxY, 0).setColor(color);
        bufferBuilder.addVertex(matrix, maxX, minY, 0).setColor(color);
        bufferBuilder.addVertex(matrix, minX, minY, 0).setColor(color);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public void bobView(PoseStack poseStack, float partialTicks) {
        if (Minecrft.get().getCameraEntity() instanceof Player pl) {
            float f = pl.walkDist - pl.walkDistO;
            float f1 = -(pl.walkDist + f * partialTicks);
            float f2 = Mth.lerp(partialTicks, pl.oBob, pl.bob);
            poseStack.translate((Mth.sin(f1 * (float) Math.PI) * f2 * 16F), (-Math.abs(Mth.cos(f1 * (float) Math.PI) * f2 * 32F)), 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * f2 * 3.0F));
        }
    }
}
