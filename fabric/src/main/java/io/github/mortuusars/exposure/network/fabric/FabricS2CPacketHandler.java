package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FabricS2CPacketHandler {
    @SuppressWarnings("unchecked")
    public static void register() {
        for (var definition : S2CPackets.getDefinitions()) {
            ClientPlayNetworking.registerGlobalReceiver(
                    (CustomPacketPayload.Type<IPacket>) definition.type(), FabricS2CPacketHandler::handleClientboundPacket);
        }

        for (var definition : CommonPackets.getDefinitions()) {
            ClientPlayNetworking.registerGlobalReceiver(
                    (CustomPacketPayload.Type<IPacket>) definition.type(), FabricS2CPacketHandler::handleClientboundPacket);
        }
    }

    private static <T extends IPacket> void handleClientboundPacket(T payload, ClientPlayNetworking.Context context) {
        payload.handle(PacketFlow.CLIENTBOUND, context.player());
    }
}
