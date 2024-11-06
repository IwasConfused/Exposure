package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.core.image.Image;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

class RenderedImageInstance implements AutoCloseable {
    private Image image;
    private DynamicTexture texture;
    private final RenderType renderType;
    private boolean requiresUpload = true;

    RenderedImageInstance(Image image) {
        this.image = image;
        this.texture = new DynamicTexture(image.getWidth(), image.getHeight(), true);
        ResourceLocation resourcelocation = Minecraft.getInstance().getTextureManager().register(image.id(), this.texture);
        this.renderType = RenderType.text(resourcelocation);
    }

    public void replaceData(Image exposure) {
        boolean hasChanged = !this.image.id().equals(exposure.id());
        this.image = exposure;
        if (hasChanged) {
            this.texture = new DynamicTexture(exposure.getWidth(), exposure.getHeight(), true);
            forceUpload();
        }
    }

    public void forceUpload() {
        this.requiresUpload = true;
    }

    protected void updateTexture() {
        if (texture.getPixels() == null)
            return;

        for (int y = 0; y < this.image.getHeight(); y++) {
            for (int x = 0; x < this.image.getWidth(); x++) {
                int ABGR = this.image.getPixelABGR(x, y);
                this.texture.getPixels().setPixelRGBA(x, y, ABGR); // Texture is in BGR format
            }
        }

        this.texture.upload();
    }

    public void draw(PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
        }

        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.renderType);
        vertexConsumer.addVertex(matrix4f, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
    }

    public void close() {
        this.texture.close();
    }
}
