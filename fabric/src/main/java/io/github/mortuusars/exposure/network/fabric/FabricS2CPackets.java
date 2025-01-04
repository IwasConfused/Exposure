package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FabricS2CPackets {
    @SuppressWarnings("unchecked")
    public static void register() {
        for (var definition : S2CPackets.getDefinitions()) {
            PayloadTypeRegistry.playS2C().register(
                    (CustomPacketPayload.Type<CustomPacketPayload>) definition.type(),
                    (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) definition.codec());
        }

        for (var definition : CommonPackets.getDefinitions()) {
            PayloadTypeRegistry.playS2C().register(
                    (CustomPacketPayload.Type<CustomPacketPayload>) definition.type(),
                    (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) definition.codec());
        }
    }
}
