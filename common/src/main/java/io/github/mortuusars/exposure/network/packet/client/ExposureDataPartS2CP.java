package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ExposureDataPartS2CP(String exposureId, byte[] partBytes, boolean isLast) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_part_to_client");
    public static final Type<ExposureDataPartS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExposureDataPartS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ExposureDataPartS2CP::exposureId,
            ByteBufCodecs.byteArray(ServersideExposureSender.TO_CLIENT_PACKET_SPLIT_THRESHOLD), ExposureDataPartS2CP::partBytes,
            ByteBufCodecs.BOOL, ExposureDataPartS2CP::isLast,
            ExposureDataPartS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureClient.exposureReceiver().receivePart(exposureId, partBytes, isLast);
        return true;
    }
}
