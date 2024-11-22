package io.github.mortuusars.exposure.client.render.photograph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.image.ModifiedImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.render.texture.TextureRenderer;
import io.github.mortuusars.exposure.core.PhotographType;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.core.pixel_modifiers.PixelModifier;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class NewPhotographRenderer {
    public static void render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight) {
        render(itemStack, renderPaper, renderBackside, poseStack, bufferSource, packedLight, 255, 255, 255, 255);
    }

    public static void render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int r, int g, int b, int a) {
        if (itemStack.getItem() instanceof PhotographItem photographItem)
            renderPhotograph(poseStack, bufferSource, photographItem, itemStack, renderPaper, renderBackside, packedLight, r, g, b, a);
        else if (itemStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem)
            renderStackedPhotographs(stackedPhotographsItem, itemStack, poseStack, bufferSource, packedLight, r, g, b, a);
    }

    public static void renderPhotograph(PoseStack poseStack, MultiBufferSource bufferSource,
                                        PhotographItem photographItem, ItemStack photographStack,
                                        boolean renderPaper, boolean renderBackside, int packedLight, int r, int g, int b, int a) {

        PhotographType photographType = photographItem.getType(photographStack);
        PhotographFeatures photographFeatures = PhotographFeatures.get(photographType);

        ExposureFrame frame = photographItem.getFrame(photographStack);

        Image image = ExposureClient.createExposureImage(frame);
        if (photographFeatures.getPixelModifier() != PixelModifier.EMPTY) {
            image = new ModifiedImage(image, photographFeatures.getPixelModifier());
        }

        int paperRotation = frame.identifier().hashCode() % 4 * 90;

        if (renderPaper && photographFeatures.getPaperTexture() != PhotographTextures.EMPTY) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
            poseStack.translate(-0.5f, -0.5f, 0);

            TextureRenderer.render(poseStack, bufferSource, photographFeatures.getPaperTexture(), packedLight, r, g, b, a);

            poseStack.popPose();

            if (renderBackside) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-0.5, 0, -0.5);

                poseStack.translate(0.5f, 0.5f, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
                poseStack.translate(-0.5f, -0.5f, 0);

                TextureRenderer.render(poseStack, bufferSource, photographFeatures.getPaperTexture(),
                        packedLight, (int) (r * 0.85f), (int) (g * 0.85f), (int) (b * 0.85f), a);

                poseStack.popPose();
            }
        }

        if (renderPaper) {
            poseStack.pushPose();
            float offset = 0.0625f;
            poseStack.translate(offset, offset, 0.001);
            poseStack.scale(0.875f, 0.875f, 0.875f);
            ExposureClient.imageRenderer().render(poseStack, bufferSource, image, RenderCoordinates.DEFAULT, packedLight, r, g, b, a);
            poseStack.popPose();
        } else {
            ExposureClient.imageRenderer().render(poseStack, bufferSource, image, RenderCoordinates.DEFAULT, packedLight, r, g, b, a);
        }

        if (renderPaper && photographFeatures.getOverlayTexture() != PhotographTextures.EMPTY) {
            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
            poseStack.translate(-0.5f, -0.5f, 0);

            poseStack.translate(0, 0, 0.002);
            TextureRenderer.render(poseStack, bufferSource, photographFeatures.getOverlayTexture(), packedLight, r, g, b, a);
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

            // Top photograph:
            if (i == 0) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 0.002);
                renderPhotograph(poseStack, bufferSource, photograph.getItem(), photograph.getItemStack(),
                        true, false, packedLight, r, g, b, a);
                poseStack.popPose();
                break;
            }

            PhotographType photographType = photograph.getItem().getType(photograph.getItemStack());
            ExposureFrame frame = photograph.getItem().getFrame(photograph.getItemStack());
            PhotographFeatures photographFeatures = PhotographFeatures.get(photographType);

            int rotation = frame.identifier().hashCode() % 4 * 90;

            // Photographs below (only paper)
            float posOffset = getStackedPhotographOffset() * i;

            poseStack.pushPose();
            poseStack.translate(posOffset, posOffset, 0.002 - i / 1000f);

            poseStack.translate(0.5f, 0.5f, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            poseStack.translate(-0.5f, -0.5f, 0);

            float brightnessMul = 1f - (getStackedBrightnessStep() * i);

            TextureRenderer.render(poseStack, bufferSource, photographFeatures.getPaperTexture(),
                    packedLight, (int)(r * brightnessMul), (int)(g * brightnessMul), (int)(b * brightnessMul), a);

            poseStack.popPose();
        }
    }

    public static float getStackedBrightnessStep() {
        return 0.15f;
    }

    public static float getStackedPhotographOffset() {
        // 2 px / Texture size (64px) = 0.03125
        return 0.03125f;
    }
}