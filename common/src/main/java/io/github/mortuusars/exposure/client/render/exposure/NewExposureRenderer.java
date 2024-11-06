package io.github.mortuusars.exposure.client.render.exposure;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.render.image.RenderedImageProvider;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.core.pixel_modifiers.PixelModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewExposureRenderer implements AutoCloseable {
    public record ExposureRenderProperties(float minX, float minY, float maxX, float maxY,
                                           float minU, float minV, float maxU, float maxV, int r, int g, int b, int a) {
        public static final ExposureRenderProperties DEFAULT = new ExposureRenderProperties(0, 0, 1, 1, 0, 0, 1, 1, 255, 255, 255, 255);
    }

    private final Map<String, Instance> cache = new HashMap<>();

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderedImageProvider imageProvider, PixelModifier modifier) {
        this.render(poseStack, bufferSource, imageProvider, modifier, ExposureRenderProperties.DEFAULT, LightTexture.FULL_BRIGHT);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderedImageProvider imageProvider,
                       PixelModifier modifier, int packedLight) {
        this.render(poseStack, bufferSource, imageProvider, modifier, ExposureRenderProperties.DEFAULT, packedLight);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderedImageProvider imageProvider,
                       PixelModifier modifier, ExposureRenderProperties prop, int packedLight) {
        this.render(poseStack, bufferSource, imageProvider, modifier,
                prop.minX, prop.minY, prop.maxX, prop.maxY, prop.minU, prop.minV, prop.maxU, prop.maxV,
                packedLight, prop.r, prop.g, prop.b, prop.a);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderedImageProvider imageProvider, PixelModifier modifier,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
            getOrCreateInstance(imageProvider, modifier)
                    .draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    private Instance getOrCreateInstance(RenderedImageProvider imageProvider, PixelModifier modifier) {
        String instanceId = imageProvider.getInstanceId() + modifier.getIdSuffix();
        return (this.cache).compute(instanceId, (expId, expData) -> {
            if (expData == null) {
                return new Instance(expId, imageProvider.get(), modifier);
            } else {
                expData.replaceData(imageProvider.get());
                return expData;
            }
        });
    }

    public void clearData() {
        for (Instance instance : cache.values()) {
            instance.close();
        }

        cache.clear();
    }

    public void clearDataSingle(@NotNull String exposureId, boolean allVariants) {
        // Using cache.entrySet().removeIf(...) would be simpler, but it wouldn't let us .close() the instance
        for(Iterator<Map.Entry<String, Instance>> it = cache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Instance> entry = it.next();
            if(allVariants ? entry.getKey().startsWith(exposureId) : entry.getKey().equals(exposureId)) {
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

    static class Instance implements AutoCloseable {
        private final RenderType renderType;

        private Image image;
        private DynamicTexture texture;
        private final PixelModifier pixelModifier;
        private boolean requiresUpload = true;

        Instance(String id, Image image, PixelModifier modifier) {
            this.image = image;
            this.texture = new DynamicTexture(image.getWidth(), image.getHeight(), true);
            this.pixelModifier = modifier;
            String textureId = createTextureId(id);
            ResourceLocation resourcelocation = Minecraft.getInstance().getTextureManager().register(textureId, this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        private static String createTextureId(String imageId) {
            String id = Exposure.ID + "/" + imageId.toLowerCase();
            id = id.replace(':', '_');

            // Player nicknames can have non az09 chars
            // we need to remove all invalid chars from the imageId to create ResourceLocation,
            // otherwise it crashes
            Pattern pattern = Pattern.compile("[^a-z0-9_.-]");
            Matcher matcher = pattern.matcher(id);

            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sb, String.valueOf(matcher.group().hashCode()));
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        private void replaceData(Image exposure) {
            boolean hasChanged = !this.image.id().equals(exposure.id());
            this.image = exposure;
            if (hasChanged) {
                this.texture = new DynamicTexture(exposure.getWidth(), exposure.getHeight(), true);
            }
            this.requiresUpload |= hasChanged;
        }

        @SuppressWarnings("unused")
        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            if (texture.getPixels() == null)
                return;

            for (int y = 0; y < this.image.getHeight(); y++) {
                for (int x = 0; x < this.image.getWidth(); x++) {
                    int ABGR = this.image.getPixelABGR(x, y);
                    ABGR = pixelModifier.modifyPixel(ABGR);
                    this.texture.getPixels().setPixelRGBA(x, y, ABGR); // Texture is in BGR format
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
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
}
