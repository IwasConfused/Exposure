package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
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

public record ExposureDataPartC2SP(ExposureIdentifier identifier, byte[] partBytes, boolean isLast) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_part_to_server");
    public static final Type<ExposureDataPartC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExposureDataPartC2SP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, ExposureDataPartC2SP::identifier,
            ByteBufCodecs.byteArray(ServersideExposureSender.TO_CLIENT_PACKET_SPLIT_THRESHOLD), ExposureDataPartC2SP::partBytes,
            ByteBufCodecs.BOOL, ExposureDataPartC2SP::isLast,
            ExposureDataPartC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        //TODO: log player name for invalid exposures
        ExposureServer.exposureReceiver().receivePart(identifier, partBytes, isLast);
        return true;
    }
}
