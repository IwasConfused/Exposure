package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.core.frame.Frame;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

public class ItemFramePhotographRenderer {
    public static void render(ItemFrame itemFrame, PoseStack poseStack, MultiBufferSource bufferSource,
                                 int packedLight, PhotographItem item, ItemStack stack) {
        if (itemFrame.getType() == EntityType.GLOW_ITEM_FRAME)
            packedLight = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();

        String entityName = BuiltInRegistries.ENTITY_TYPE.getKey(itemFrame.getType()).toString();
        if (entityName.equals("quark:glass_frame")) {
            poseStack.translate(0, 0, 0.475f);
        }

        // Snap to 90 degrees like a map.
        poseStack.mulPose(Axis.ZP.rotationDegrees(45 * itemFrame.getRotation()));

        float pixelSize = 0.0625f;
        float scale = 1f - pixelSize * 6; // 3px from each side

        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, -0.5, 10);

        ExposureClient.photographRenderer().renderPhotograph(poseStack, bufferSource, item, stack,
                false, false, packedLight, 255, 255, 255, 255);

        poseStack.popPose();
    }
}
