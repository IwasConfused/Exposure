package io.github.mortuusars.exposure.neoforge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.command.ExposureCommand;
import io.github.mortuusars.exposure.command.ShaderCommand;
import io.github.mortuusars.exposure.command.TestCommand;
import io.github.mortuusars.exposure.data.lenses.Lenses;
import io.github.mortuusars.exposure.data.lenses.LensesDataLoader;
import io.github.mortuusars.exposure.network.neoforge.PacketsImpl;
import io.github.mortuusars.exposure.network.packet.client.*;
import io.github.mortuusars.exposure.network.packet.server.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@SuppressWarnings("unused")
public class CommonEvents {
    @EventBusSubscriber(modid = Exposure.ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                // Makes stats show up in stat screen
                Exposure.Stats.STATS.forEach((location, statFormatter) -> {
                    Stats.CUSTOM.get(location);
                });
            });
        }

        @SuppressWarnings("unused")
        @SubscribeEvent
        public static void onRegisterPackets(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");

            registrar.playToClient(ApplyShaderS2CP.TYPE, ApplyShaderS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(ClearRenderingCacheS2CP.TYPE, ClearRenderingCacheS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(CreateChromaticExposureS2CP.TYPE, CreateChromaticExposureS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(ExposeCommandS2CP.TYPE, ExposeCommandS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(ExposureChangedS2CP.TYPE, ExposureChangedS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(ExposureDataPartS2CP.TYPE, ExposureDataPartS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(ExposureDataS2CP.TYPE, ExposureDataS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(LoadExposureFromFileCommandS2CP.TYPE, LoadExposureFromFileCommandS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(OnFrameAddedS2CP.TYPE, OnFrameAddedS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(PlayOnePerEntitySoundS2CP.TYPE, PlayOnePerEntitySoundS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(ShowExposureCommandS2CP.TYPE, ShowExposureCommandS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(StartExposureS2CP.TYPE, StartExposureS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(StopOnePerEntitySoundS2CP.TYPE, StopOnePerEntitySoundS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(SyncLensesDataS2CP.TYPE, SyncLensesDataS2CP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToClient(WaitForExposureChangeS2CP.TYPE, WaitForExposureChangeS2CP.STREAM_CODEC, PacketsImpl::handle);


            registrar.playToServer(AlbumSignC2SP.TYPE, AlbumSignC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(AlbumSyncNoteC2SP.TYPE, AlbumSyncNoteC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(CameraAddFrameC2SP.TYPE, CameraAddFrameC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(CameraSetCompositionGuideC2SP.TYPE, CameraSetCompositionGuideC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(CameraSetFlashModeC2SP.TYPE, CameraSetFlashModeC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(CameraSetSelfieModeC2SP.TYPE, CameraSetSelfieModeC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(CameraSetShutterSpeedC2SP.TYPE, CameraSetShutterSpeedC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(CameraSetZoomC2SP.TYPE, CameraSetZoomC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(DeactivateCameraC2SP.TYPE, DeactivateCameraC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(ExposureDataPartC2SP.TYPE, ExposureDataPartC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(OpenCameraAttachmentsInCreativePacketC2SP.TYPE, OpenCameraAttachmentsInCreativePacketC2SP.STREAM_CODEC, PacketsImpl::handle);
            registrar.playToServer(QueryExposureDataC2SP.TYPE, QueryExposureDataC2SP.STREAM_CODEC, PacketsImpl::handle);
        }

        @SubscribeEvent
        public static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(Exposure.Items.CAMERA.get());
                event.accept(Exposure.Items.BLACK_AND_WHITE_FILM.get());
                event.accept(Exposure.Items.COLOR_FILM.get());
                event.accept(Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get());
                event.accept(Exposure.Items.DEVELOPED_COLOR_FILM.get());
                event.accept(Exposure.Items.PHOTOGRAPH.get());
                event.accept(Exposure.Items.AGED_PHOTOGRAPH.get());
                event.accept(Exposure.Items.INTERPLANAR_PROJECTOR.get());
                event.accept(Exposure.Items.STACKED_PHOTOGRAPHS.get());
                event.accept(Exposure.Items.PHOTOGRAPH_FRAME.get());
                event.accept(Exposure.Items.ALBUM.get());
            }

            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                event.accept(Exposure.Items.LIGHTROOM.get());
            }
        }

        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            //TODO: Capabilities
//            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Exposure.BlockEntityTypes.LIGHTROOM.get(),
//                    (blockEntity, side) ->
////                            ((LightroomBlockEntityForgeMixin) blockEntity)
//            );
        }
    }

    @EventBusSubscriber(modid = Exposure.ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameBus {
        @SubscribeEvent
        public static void serverStarting(ServerStartingEvent event) {
            Exposure.initServer(event.getServer());
        }

        @SubscribeEvent
        public static void addReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new LensesDataLoader());
        }

        @SubscribeEvent
        public static void onDatapackSync(OnDatapackSyncEvent event) {
            Lenses.onDatapackSync(event.getPlayer());
        }

        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            ExposureCommand.register(event.getDispatcher());
            ShaderCommand.register(event.getDispatcher());
            TestCommand.register(event.getDispatcher());
        }
    }
}
