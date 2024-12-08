package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.image.ImageIdentifier;
import io.github.mortuusars.exposure.client.image.RenderableImage;
import io.github.mortuusars.exposure.core.image.color.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ImageRenderer implements AutoCloseable {
    private final Map<ImageIdentifier, RenderedImageInstance> cache = new HashMap<>();

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderableImage image) {
        this.render(poseStack, bufferSource, image, RenderCoordinates.DEFAULT, LightTexture.FULL_BRIGHT, Color.WHITE);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderableImage image, int packedLight) {
        this.render(poseStack, bufferSource, image, RenderCoordinates.DEFAULT, packedLight, Color.WHITE);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderableImage image, RenderCoordinates coords, Color color) {
        this.render(poseStack, bufferSource, image, coords, LightTexture.FULL_BRIGHT, color);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderableImage image, RenderCoordinates coords,
                       int packedLight, Color color) {
        this.render(poseStack, bufferSource, image,
                coords.minX(), coords.minY(), coords.maxX(), coords.maxY(), coords.minU(), coords.minV(), coords.maxU(), coords.maxV(),
                packedLight, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderableImage image, RenderCoordinates coords,
                       int packedLight, int r, int g, int b, int a) {
        this.render(poseStack, bufferSource, image,
                coords.minX(), coords.minY(), coords.maxX(), coords.maxY(), coords.minU(), coords.minV(), coords.maxU(), coords.maxV(),
                packedLight, r, g, b, a);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderableImage image,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        getOrCreateInstance(image)
                .draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    private RenderedImageInstance getOrCreateInstance(RenderableImage image) {
        return (this.cache).compute(image.getIdentifier(), (id, expData) -> {
            if (expData == null) {
                return new RenderedImageInstance(image);
            }
            expData.replaceData(image);
            return expData;
        });
    }

    public void clearCache() {
        cache.values().forEach(RenderedImageInstance::close);
        cache.clear();
    }

    public void clearCacheOf(Predicate<ImageIdentifier> predicate) {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = predicate.test(entry.getKey());
            if (shouldRemove) {
                entry.getValue().close();
            }
            return shouldRemove;
        });
    }

    @Override
    public void close() {
        clearCache();
    }
}
