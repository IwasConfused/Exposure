package io.github.mortuusars.exposure.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.mortuusars.exposure.util.Rect2f;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GuiUtil {
    public static void blit(PoseStack poseStack, Rect2f rect,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(null, poseStack, rect, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(@Nullable ResourceLocation texture, PoseStack poseStack, Rect2f rect,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(texture, poseStack, rect.x, rect.y, rect.width, rect.height, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(PoseStack poseStack, float x, float y, float width, float height,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(null, poseStack, x, y, width, height, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(@Nullable ResourceLocation texture, PoseStack poseStack, float x, float y, float width, float height,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(texture, poseStack, x, x + width, y, y + height, zOffset,
                u / (float)textureWidth, (u + width) / (float)textureWidth,
                v / (float)textureHeight, (v + height) / (float)textureHeight);
    }

    public static void blit(PoseStack poseStack, float minX, float maxX, float minY, float maxY, float zOffset,
                            float minU, float maxU, float minV, float maxV) {
        blit(null, poseStack, minX, maxX, minY, maxY, zOffset, minU, maxU, minV, maxV);
    }

    private static void blit(@Nullable ResourceLocation texture, PoseStack poseStack,
                             float minX, float maxX, float minY, float maxY, float zOffset,
                             float minU, float maxU, float minV, float maxV) {
        if (texture != null) {
            RenderSystem.setShaderTexture(0, texture);
        }

        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, minX, maxY, zOffset).setUv(minU, maxV);
        bufferBuilder.addVertex(matrix, maxX, maxY, zOffset).setUv(maxU, maxV);
        bufferBuilder.addVertex(matrix, maxX, minY, zOffset).setUv(maxU, minV);
        bufferBuilder.addVertex(matrix, minX, minY, zOffset).setUv(minU, minV);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    // --

    public static void drawRect(GuiGraphics guiGraphics, Rect2f rect, int color) {
        drawRect(guiGraphics, rect.x, rect.y, rect.width, rect.height, color);
    }

    public static void drawRect(GuiGraphics guiGraphics, float x, float y, float width, float height, int color) {
        drawRect(guiGraphics.pose(), x, y, x + width, y + height, color);
    }

    public static void drawRect(PoseStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
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

        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(matrix, minX, maxY, 0).setColor(color);
        bufferBuilder.addVertex(matrix, maxX, maxY, 0).setColor(color);
        bufferBuilder.addVertex(matrix, maxX, minY, 0).setColor(color);
        bufferBuilder.addVertex(matrix, minX, minY, 0).setColor(color);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}
