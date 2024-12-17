package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.common.DeactivateActiveCameraCommonPacket;
import io.github.mortuusars.exposure.network.packet.server.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class CommonPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                new CustomPacketPayload.TypeAndCodec<>(DeactivateActiveCameraCommonPacket.TYPE, DeactivateActiveCameraCommonPacket.STREAM_CODEC)
        );
    }
}
