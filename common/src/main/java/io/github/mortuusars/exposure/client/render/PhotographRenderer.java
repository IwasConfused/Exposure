package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.client.render.image.RenderedImageProvider;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class PhotographRenderer {
    public static void render(ItemStack stack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int r, int g, int b, int a) {
        if (stack.getItem() instanceof PhotographItem photographItem)
            renderPhotograph(photographItem, stack, renderPaper, renderBackside, poseStack, bufferSource, packedLight, r, g, b, a);
        else if (stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem)
            renderStackedPhotographs(stackedPhotographsItem, stack, poseStack, bufferSource, packedLight, r, g, b, a);
    }

    public static void renderPhotograph(PhotographItem photographItem, ItemStack stack, boolean renderPaper,
                                        boolean renderBackside, PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, int r, int g, int b, int a) {
        PhotographRenderProperties properties = PhotographRenderProperties.get(stack);
        int size = ExposureClient.exposureRenderer().getSize();
        float rotateOffset = size / 2f;

        ExposureFrame frame = photographItem.getFrame(stack);
        RenderedImageProvider imageProvider = frame != null ? RenderedImageProvider.fromFrame(frame) : RenderedImageProvider.EMPTY;

        int rotation = imageProvider.getInstanceId().hashCode() % 4;

        if (renderPaper) {
            poseStack.pushPose();
            poseStack.translate(rotateOffset, rotateOffset, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 90));
            poseStack.translate(-rotateOffset, -rotateOffset, 0);

            renderTexture(properties.getPaperTexture(), poseStack, bufferSource, 0, 0,
                    size, size, packedLight, r, g, b, a);

            poseStack.popPose();

            if (renderBackside) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-size, 0, -0.5);

                poseStack.translate(rotateOffset, rotateOffset, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 90));
                poseStack.translate(-rotateOffset, -rotateOffset, 0);

                renderTexture(properties.getPaperTexture(), poseStack, bufferSource,
                        packedLight, (int) (r * 0.85f), (int) (g * 0.85f), (int) (b * 0.85f), a);

                poseStack.popPose();
            }
        }

        if (renderPaper) {
            poseStack.pushPose();
            float offset = size * 0.0625f;
            poseStack.translate(offset, offset, 1);
            poseStack.scale(0.875f, 0.875f, 0.875f);
            ExposureClient.exposureRenderer().render(imageProvider, properties.getModifier(), poseStack, bufferSource,
                    packedLight, r, g, b, a);
            poseStack.popPose();
        } else {
            ExposureClient.exposureRenderer().render(imageProvider, properties.getModifier(), poseStack, bufferSource,
                    packedLight, r, g, b, a);
        }

        if (renderPaper && properties.hasPaperOverlayTexture()) {
            poseStack.pushPose();

            poseStack.translate(rotateOffset, rotateOffset, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 90));
            poseStack.translate(-rotateOffset, -rotateOffset, 0);

            poseStack.translate(0, 0, 2);
            renderTexture(properties.getPaperOverlayTexture(), poseStack, bufferSource, packedLight, r, g, b, a);
            poseStack.popPose();
        }
    }

    public static void renderStackedPhotographs(StackedPhotographsItem stackedPhotographsItem, ItemStack stack,
                                                PoseStack poseStack, MultiBufferSource bufferSource,
                                                int packedLight, int r, int g, int b, int a) {
        List<ItemAndStack<PhotographItem>> photographs = stackedPhotographsItem.getPhotographs(stack);
        renderStackedPhotographs(photographs, poseStack, bufferSource, packedLight, r, g, b, a);
    }

    public static void renderStackedPhotographs(List<ItemAndStack<PhotographItem>> photographs,
                                                PoseStack poseStack, MultiBufferSource bufferSource,
                                                int packedLight, int r, int g, int b, int a) {
        if (photographs.isEmpty())
            return;

        for (int i = 2; i >= 0; i--) {
            if (photographs.size() - 1 < i)
                continue;

            ItemAndStack<PhotographItem> photograph = photographs.get(i);
            PhotographRenderProperties properties = PhotographRenderProperties.get(photograph.getItemStack());

            // Top photograph:
            if (i == 0) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 2);
                renderPhotograph(photograph.getItem(), photograph.getItemStack(), true, false, poseStack,
                        bufferSource, packedLight, r, g, b, a);
                poseStack.popPose();
                break;
            }

            ExposureFrame frame = photograph.getItem().getFrame(photograph.getItemStack());
            RenderedImageProvider imageProvider = frame != null ? RenderedImageProvider.fromFrame(frame) : RenderedImageProvider.EMPTY;

            int rotation = imageProvider.getInstanceId().hashCode() % 4;

            // Photographs below (only paper)
            float posOffset = getStackedPhotographOffset() * i;
            float rotateOffset = ExposureClient.exposureRenderer().getSize() / 2f;

            poseStack.pushPose();
            poseStack.translate(posOffset, posOffset, 2 - i);

            poseStack.translate(rotateOffset, rotateOffset, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 90));
            poseStack.translate(-rotateOffset, -rotateOffset, 0);

            float brightnessMul = 1f - (getStackedBrightnessStep() * i);

            renderTexture(properties.getPaperTexture(), poseStack, bufferSource,
                    packedLight, (int)(r * brightnessMul), (int)(g * brightnessMul), (int)(b * brightnessMul), a);

            poseStack.popPose();
        }
    }

    public static float getStackedBrightnessStep() {
        return 0.15f;
    }

    public static float getStackedPhotographOffset() {
        // 2 px / Texture size (64px) = 0.03125
        return ExposureClient.exposureRenderer().getSize() * 0.03125f;
    }

    private static void renderTexture(ResourceLocation resource, PoseStack poseStack, MultiBufferSource bufferSource,
                                      int packedLight, int r, int g, int b, int a) {
        renderTexture(resource, poseStack, bufferSource, 0, 0, ExposureClient.exposureRenderer().getSize(),
                ExposureClient.exposureRenderer().getSize(), packedLight, r, g, b, a);
    }

    private static void renderTexture(ResourceLocation resource, PoseStack poseStack, MultiBufferSource bufferSource,
                                      float x, float y, float width, float height, int packedLight, int r, int g, int b, int a) {
        renderTexture(resource, poseStack, bufferSource, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    private static void renderTexture(ResourceLocation resource, PoseStack poseStack, MultiBufferSource bufferSource,
                                      float minX, float minY, float maxX, float maxY,
                                      float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer bufferBuilder = bufferSource.getBuffer(RenderType.text(resource));
        bufferBuilder.addVertex(matrix, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
    }
}