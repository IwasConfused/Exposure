package io.github.mortuusars.exposure.neoforge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.core.camera.ZoomDirection;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.ClientTrichromeFinalizer;
import io.github.mortuusars.exposure.client.ExposureClientReloadListener;
import io.github.mortuusars.exposure.client.MouseHandler;
import io.github.mortuusars.exposure.data.filter.Filters;
import io.github.mortuusars.exposure.client.gui.component.PhotographTooltip;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.render.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.client.render.PhotographFrameEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.level.LevelEvent;

@SuppressWarnings("unused")
public class ClientEvents {
    @EventBusSubscriber(modid = Exposure.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(ExposureClient::init);
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(Exposure.MenuTypes.CAMERA.get(), CameraAttachmentsScreen::new);
            event.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
            event.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
            event.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
            event.register(Exposure.MenuTypes.ITEM_RENAME.get(), ItemRenameScreen::new);
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Exposure.EntityTypes.PHOTOGRAPH_FRAME.get(), PhotographFrameEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(PhotographTooltip.class, photographTooltip -> photographTooltip);
        }

        @SubscribeEvent
        public static void registerResourceReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new ExposureClientReloadListener());
            event.registerReloadListener(new Filters.Loader());
        }

        @SubscribeEvent
        public static void registerModels(ModelEvent.RegisterAdditional event) {
            event.register(ExposureClient.Models.CAMERA_GUI);
            event.register(ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL);
            event.register(ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL_STRIPPED);
            event.register(ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM);
            event.register(ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM_STRIPPED);
            event.register(ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE);
            event.register(ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE_STRIPPED);
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            ExposureClient.registerKeymappings(key -> {
                event.register(key);
                return key;
            });
        }
    }

    @EventBusSubscriber(modid = Exposure.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameBus {
        @SubscribeEvent
        public static void onLevelClear(LevelEvent.Unload event) {
            ExposureClient.exposureRenderer().clearData();
        }

        @SubscribeEvent
        public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ExposureClient.exposureCache().clear();
            ExposureClient.exposureReceiver().clear();
        }

        @SubscribeEvent
        public static void renderOverlay(RenderGuiEvent.Pre event) {
            if (Viewfinder.isLookingThrough())
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void mouseScroll(InputEvent.MouseScrollingEvent event) {
            if (Viewfinder.handleMouseScroll(event.getScrollDeltaY() > 0d ? ZoomDirection.IN : ZoomDirection.OUT))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void computeFOV(ViewportEvent.ComputeFov event) {
            if (!event.usedConfiguredFov())
                return;

            double prevFov = event.getFOV();
            double modifiedFov = Viewfinder.modifyFov(prevFov);
            if (prevFov != modifiedFov)
                event.setFOV(modifiedFov);
        }

        @SubscribeEvent
        public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
            if (MouseHandler.handleMouseButtonPress(event.getButton(), event.getAction(), event.getModifiers()))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void renderItemFrameItem(RenderItemInFrameEvent event) {
            if (ItemFramePhotographRenderer.render(event.getItemFrameEntity(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onRenderTick(RenderFrameEvent.Post event) {
            CaptureManager.onRenderTickEnd();
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            ClientTrichromeFinalizer.clientTick();
        }
    }
}
