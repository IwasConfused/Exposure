package io.github.mortuusars.exposure.neoforge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.ExposureClientReloadListener;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.data.filter.Filters;
import io.github.mortuusars.exposure.client.gui.tooltip.PhotographClientTooltip;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.render.PhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.item.tooltip.PhotographTooltip;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;

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
            event.register(PhotographTooltip.class, PhotographClientTooltip::new);
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
            KeyboardHandler.registerKeymappings(key -> {
                event.register(key);
                return key;
            });
        }
    }

//    @EventBusSubscriber(modid = Exposure.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
//    public static class GameBus {
//
//    }
}
