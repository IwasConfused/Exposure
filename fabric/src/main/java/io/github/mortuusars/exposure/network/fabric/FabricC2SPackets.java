package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FabricC2SPackets {
    @SuppressWarnings("unchecked")
    public static void register() {
        // This monstrosity is to avoid having to define packets for forge and fabric separately.
        for (var definition : S2CPackets.getDefinitions()) {
            PayloadTypeRegistry.playC2S().register(
                    (CustomPacketPayload.Type<CustomPacketPayload>) definition.type(),
                    (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) definition.codec().cast());
            ServerPlayNetworking.registerGlobalReceiver((CustomPacketPayload.Type<IPacket>) definition.type(), FabricC2SPackets::handleServerboundPacket);
        }

        for (var definition : CommonPackets.getDefinitions()) {
            PayloadTypeRegistry.playC2S().register(
                    (CustomPacketPayload.Type<CustomPacketPayload>) definition.type(),
                    (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) definition.codec().cast());
            ServerPlayNetworking.registerGlobalReceiver((CustomPacketPayload.Type<IPacket>) definition.type(), FabricC2SPackets::handleServerboundPacket);
        }
    }

    private static <T extends IPacket> void handleServerboundPacket(T payload, ServerPlayNetworking.Context context) {
        payload.handle(PacketFlow.SERVERBOUND, context.player());
    }
}
