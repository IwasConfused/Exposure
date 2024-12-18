package io.github.mortuusars.exposure.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class GuiUtil {
    public static void blit(PoseStack poseStack, float minX, float maxX, float minY, float maxY, float blitOffset, float minU, float maxU, float minV, float maxV) {
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, minX, maxY, blitOffset).setUv(minU, maxV);
        bufferBuilder.addVertex(matrix, maxX, maxY, blitOffset).setUv(maxU, maxV);
        bufferBuilder.addVertex(matrix, maxX, minY, blitOffset).setUv(maxU, minV);
        bufferBuilder.addVertex(matrix, minX, minY, blitOffset).setUv(minU, minV);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    public static void blit(PoseStack poseStack, float x, float y, float width, float height, int u, int v, int textureWidth, int textureHeight, float blitOffset) {
        blit(poseStack, x, x + width, y, y + height, blitOffset,
                (u + 0.0F) / (float)textureWidth, (u + width) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + height) / (float)textureHeight);
    }
}
