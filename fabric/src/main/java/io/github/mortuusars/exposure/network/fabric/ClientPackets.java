package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.client.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.PacketFlow;

public class ClientPackets {
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ApplyShaderS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ClearRenderingCacheS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(CreateChromaticExposureS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ExposeCommandS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ExposureChangedS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataPartS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(LoadExposureFromFileCommandS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(OnFrameAddedS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(PlayOnePerPlayerSoundS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(ShowExposureCommandS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(StartExposureS2CP.TYPE, ClientPackets::handleClientboundPacket);
        ClientPlayNetworking.registerGlobalReceiver(StopOnePerPlayerSoundS2CP.TYPE, ClientPackets::handleClientboundPacket);
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
