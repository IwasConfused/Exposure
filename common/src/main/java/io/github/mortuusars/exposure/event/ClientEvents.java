package io.github.mortuusars.exposure.event;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderRegistry;
import io.github.mortuusars.exposure.client.capture.template.CameraCaptureTemplate;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.client.capture.template.PreloadingDummyCaptureTemplate;
import io.github.mortuusars.exposure.client.render.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.client.sound.OnePerEntitySoundsClient;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

public class ClientEvents {
    public static void levelUnloaded() {
    }

    public static void login() {
        try {
            preloadStuffToFixLagSpikes();
        } catch (Exception e) {
            Exposure.LOGGER.warn("Failed to preload stuff: {}", e.getMessage());
        }
    }

    private static void preloadStuffToFixLagSpikes() {
        ClientPacketsHandler.clearRenderingCache();
        boolean active = Minecrft.player().getActiveExposureCamera().isEmpty();

        EasingFunction.EASE_OUT_EXPO.ease(1);
        ViewfinderRegistry.getOrThrow(Exposure.Items.CAMERA.get()).apply(new Camera(Minecrft.player(), CameraID.createRandom()) {
            @Override
            public ItemStack getItemStack() {
                return new ItemStack(Exposure.Items.CAMERA.get());
            }
        });
        CameraClient.removeViewfinder();

        CaptureTemplates.get(Exposure.resource("dummy"));
        CameraCaptureTemplate cameraCaptureTemplate = new CameraCaptureTemplate();
        ExposureClient.cycles().enqueueTask(new PreloadingDummyCaptureTemplate().createTask(null));
        OnePerEntitySoundsClient.stop(Minecrft.player(), Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get());
    }

    public static void disconnect() {
        resetRenderData();
    }

    public static void resetRenderData() {
        ExposureClient.exposureStore().clear();
        ExposureClient.renderedExposures().clearCache();
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

    public static void resourcesReloaded() {
        ExposureClient.exposureStore().clear();
        ExposureClient.renderedExposures().clearCache();
        ExposureClient.imageRenderer().clearCache();
    }
}
