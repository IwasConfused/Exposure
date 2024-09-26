package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.core.image.IImage;
import io.github.mortuusars.exposure.client.render.image.RenderedImageProvider;
import io.github.mortuusars.exposure.core.pixel_modifiers.IPixelModifier;
import net.minecraft.client.Minecraft;
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

public class ExposureRenderer implements AutoCloseable {
    private final Map<String, ExposureInstance> cache = new HashMap<>();

    public int getSize() {
        return 256;
    }

    public void render(@NotNull RenderedImageProvider imageProvider, IPixelModifier modifier,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int r, int g, int b, int a) {
        render(imageProvider, modifier, poseStack, bufferSource, 0, 0, getSize(), getSize(), packedLight, r, g, b, a);
    }

    public void render(@NotNull RenderedImageProvider imageProvider, IPixelModifier modifier,
                       PoseStack poseStack, MultiBufferSource bufferSource, float x, float y, float width, float height,
                       int packedLight, int r, int g, int b, int a) {
        render(imageProvider, modifier, poseStack, bufferSource, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public void render(@NotNull RenderedImageProvider imageProvider, IPixelModifier modifier,
                       PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
            getOrCreateExposureInstance(imageProvider, modifier)
                    .draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    private ExposureInstance getOrCreateExposureInstance(RenderedImageProvider imageProvider, IPixelModifier modifier) {
        String instanceId = imageProvider.getInstanceId() + modifier.getIdSuffix();
        return (this.cache).compute(instanceId, (expId, expData) -> {
            if (expData == null) {
                return new ExposureInstance(expId, imageProvider.get(), modifier);
            } else {
                expData.replaceData(imageProvider.get());
                return expData;
            }
        });
    }

    public void clearData() {
        for (ExposureInstance instance : cache.values()) {
            instance.close();
        }

        cache.clear();
    }

    public void clearDataSingle(@NotNull String exposureId, boolean allVariants) {
        // Using cache.entrySet().removeIf(...) would be simpler, but it wouldn't let us .close() the instance
        for(Iterator<Map.Entry<String, ExposureInstance>> it = cache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ExposureInstance> entry = it.next();
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

    static class ExposureInstance implements AutoCloseable {
        private final RenderType renderType;

        private IImage exposure;
        private DynamicTexture texture;
        private final IPixelModifier pixelModifier;
        private boolean requiresUpload = true;

        ExposureInstance(String id, IImage exposure, IPixelModifier modifier) {
            this.exposure = exposure;
            this.texture = new DynamicTexture(exposure.getWidth(), exposure.getHeight(), true);
            this.pixelModifier = modifier;
            String textureId = createTextureId(id);
            ResourceLocation resourcelocation = Minecraft.getInstance().getTextureManager().register(textureId, this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        private static String createTextureId(String exposureId) {
            String id = "exposure/" + exposureId.toLowerCase();
            id = id.replace(':', '_');

            // Player nicknames can have non az09 chars
            // we need to remove all invalid chars from the exposureId to create ResourceLocation,
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

        private void replaceData(IImage exposure) {
            boolean hasChanged = !this.exposure.getImageId().equals(exposure.getImageId());
            this.exposure = exposure;
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

            for (int y = 0; y < this.exposure.getHeight(); y++) {
                for (int x = 0; x < this.exposure.getWidth(); x++) {
                    int ABGR = this.exposure.getPixelABGR(x, y);
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
