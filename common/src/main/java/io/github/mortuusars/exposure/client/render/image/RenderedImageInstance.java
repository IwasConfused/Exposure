package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.image.IdentifiableImage;
import io.github.mortuusars.exposure.core.image.Image;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderedImageInstance implements AutoCloseable {
    protected IdentifiableImage image;
    protected DynamicTexture texture;
    protected final ResourceLocation textureLocation;
    protected final RenderType renderType;
    protected boolean requiresUpload = true;

    RenderedImageInstance(IdentifiableImage image) {
        this.image = image;
        this.texture = new DynamicTexture(image.getWidth(), image.getHeight(), true);
        // Filter id because ResourceLocation will crash if non-valid chars are present:
        String id = Exposure.ID + "/" + Util.sanitizeName(image.id(), ResourceLocation::validPathChar);
        this.textureLocation = Minecraft.getInstance().getTextureManager().register(id, this.texture);
        this.renderType = RenderType.text(textureLocation);
    }

    public void replaceData(IdentifiableImage image) {
        boolean hasChanged = !this.image.id().equals(image.id());
        this.image = image;
        if (hasChanged) {
            this.texture = new DynamicTexture(image.getWidth(), image.getHeight(), true);
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
                int ABGR = this.image.getPixelARGB(x, y);
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
        Minecraft.getInstance().getTextureManager().release(textureLocation);
        this.texture.close();
    }
}
