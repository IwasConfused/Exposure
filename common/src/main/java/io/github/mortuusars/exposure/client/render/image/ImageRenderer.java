package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.core.image.color.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageRenderer implements AutoCloseable {
    private final Map<String, RenderedImageInstance> cache = new HashMap<>();

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Image image) {
        this.render(poseStack, bufferSource, image, RenderCoordinates.DEFAULT, LightTexture.FULL_BRIGHT, Color.WHITE);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Image image, int packedLight) {
        this.render(poseStack, bufferSource, image, RenderCoordinates.DEFAULT, packedLight, Color.WHITE);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Image image, RenderCoordinates coords, Color color) {
        this.render(poseStack, bufferSource, image, coords, LightTexture.FULL_BRIGHT, color);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Image image, RenderCoordinates coords,
                       int packedLight, Color color) {
        this.render(poseStack, bufferSource, image,
                coords.minX(), coords.minY(), coords.maxX(), coords.maxY(), coords.minU(), coords.minV(), coords.maxU(), coords.maxV(),
                packedLight, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Image image, RenderCoordinates coords,
                       int packedLight, int r, int g, int b, int a) {
        this.render(poseStack, bufferSource, image,
                coords.minX(), coords.minY(), coords.maxX(), coords.maxY(), coords.minU(), coords.minV(), coords.maxU(), coords.maxV(),
                packedLight, r, g, b, a);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Image image,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
            getOrCreateInstance(image)
                    .draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    private RenderedImageInstance getOrCreateInstance(Image image) {
        return (this.cache).compute(image.id(), (id, expData) -> {
            if (expData == null) {
                return new RenderedImageInstance(image);
            } else {
                expData.replaceData(image);
                return expData;
            }
        });
    }

    public void clearData() {
        for (RenderedImageInstance instance : cache.values()) {
            instance.close();
        }

        cache.clear();
    }

    public void clearDataSingle(@NotNull String id, boolean allVariants) {
        // Using cache.entrySet().removeIf(...) would be simpler, but it wouldn't let us .close() the instance
        for(Iterator<Map.Entry<String, RenderedImageInstance>> it = cache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, RenderedImageInstance> entry = it.next();
            if(allVariants ? entry.getKey().startsWith(id) : entry.getKey().equals(id)) {
                entry.getValue().close();
                it.remove();

                if (!allVariants)
                    break;
            }
        }
    }

    @Override
    public void close() {
        clearData();
    }
}
