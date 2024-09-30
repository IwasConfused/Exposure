package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.client.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.PacketFlow;

public class ClientPackets {
    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(ApplyShaderS2CP.TYPE, ApplyShaderS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClearRenderingCacheS2CP.TYPE, ClearRenderingCacheS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(CreateChromaticExposureS2CP.TYPE, CreateChromaticExposureS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ExposeCommandS2CP.TYPE, ExposeCommandS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ExposureChangedS2CP.TYPE, ExposureChangedS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ExposureDataPartS2CP.TYPE, ExposureDataPartS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ExposureDataS2CP.TYPE, ExposureDataS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(LoadExposureFromFileCommandS2CP.TYPE, LoadExposureFromFileCommandS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OnFrameAddedS2CP.TYPE, OnFrameAddedS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PlayOnePerEntitySoundS2CP.TYPE, PlayOnePerEntitySoundS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PlayOnePerEntityShutterTickingSoundS2CP.TYPE, PlayOnePerEntityShutterTickingSoundS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ShowExposureCommandS2CP.TYPE, ShowExposureCommandS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(StartExposureS2CP.TYPE, StartExposureS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(StopOnePerEntitySoundS2CP.TYPE, StopOnePerEntitySoundS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncLensesDataS2CP.TYPE, SyncLensesDataS2CP.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(WaitForExposureChangeS2CP.TYPE, WaitForExposureChangeS2CP.STREAM_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ApplyShaderS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ClearRenderingCacheS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(CreateChromaticExposureS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ExposeCommandS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ExposureChangedS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataPartS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(LoadExposureFromFileCommandS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(OnFrameAddedS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(PlayOnePerEntitySoundS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(PlayOnePerEntityShutterTickingSoundS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ShowExposureCommandS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(StartExposureS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(StopOnePerEntitySoundS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(SyncLensesDataS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(WaitForExposureChangeS2CP.TYPE, ClientPackets::handleClientboundPacket);
    }

    private static <T extends IPacket> void handleClientboundPacket(T payload, ClientPlayNetworking.Context context) {
        payload.handle(PacketFlow.CLIENTBOUND, context.player());
    }

    public static void sendToServer(IPacket packet) {
        ClientPlayNetworking.send(packet);
    }
}
