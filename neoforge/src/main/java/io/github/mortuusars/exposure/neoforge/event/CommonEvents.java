package io.github.mortuusars.exposure.neoforge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.command.ExposureCommand;
import io.github.mortuusars.exposure.command.ShaderCommand;
import io.github.mortuusars.exposure.data.lenses.Lenses;
import io.github.mortuusars.exposure.data.lenses.LensesDataLoader;
import io.github.mortuusars.exposure.network.neoforge.PacketsImpl;
import io.github.mortuusars.exposure.network.packet.C2SPackets;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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

        @SuppressWarnings("unchecked")
        @SubscribeEvent
        public static void onRegisterPackets(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            // This monstrosity is to avoid having to define packets for forge and fabric separately.
            for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : S2CPackets.getDefinitions()) {
                registrar.playToClient((CustomPacketPayload.Type<IPacket>)definition.type(),
                        (StreamCodec<FriendlyByteBuf, IPacket>)definition.codec(), PacketsImpl::handle);
            }

            for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : C2SPackets.getDefinitions()) {
                registrar.playToServer((CustomPacketPayload.Type<IPacket>)definition.type(),
                        (StreamCodec<FriendlyByteBuf, IPacket>)definition.codec(), PacketsImpl::handle);
            }
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
        }
    }
}
