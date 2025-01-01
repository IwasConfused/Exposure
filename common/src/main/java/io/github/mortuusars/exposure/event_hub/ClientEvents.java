package io.github.mortuusars.exposure.event_hub;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.decoration.ItemFrame;

public class ClientEvents {
    public static void levelUnloaded() {
    }

    public static void disconnect() {
        ExposureClient.exposureStore().clear();
    }

    public static void resetRenderData() {
        ExposureClient.imageRenderer().clearCache();
    }

    public static boolean renderItemFrameItem(ItemFrame itemFrame, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!Config.Client.PHOTOGRAPH_RENDERS_IN_ITEM_FRAME.get()) return false;
        if (!(itemFrame.getItem().getItem() instanceof PhotographItem photographItem)) return false;
        if (photographItem.getFrame(itemFrame.getItem()).exposureIdentifier().isEmpty()) return false;

        poseStack.pushPose();
        poseStack.scale(2F, 2F, 2F);
        ItemFramePhotographRenderer.render(itemFrame, poseStack, buffer, packedLight, photographItem, itemFrame.getItem());
        poseStack.popPose();

        return true;
    }
}
