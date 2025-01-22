package io.github.mortuusars.exposure.network;

import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PacketDefinition(CustomPacketPayload.Type<?> type,
                               StreamCodec<? super RegistryFriendlyByteBuf, ? extends Packet> streamCodec) {
}
