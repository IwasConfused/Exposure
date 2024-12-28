package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.foundation.warehouse.ExposureData;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ExposureDataS2CP(ExposureIdentifier identifier, ExposureData exposureData) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("exposure_data");
    public static final CustomPacketPayload.Type<ExposureDataS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExposureDataS2CP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, ExposureDataS2CP::identifier,
            ExposureData.STREAM_CODEC, ExposureDataS2CP::exposureData,
            ExposureDataS2CP::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureClient.exposureReceiver().receive(identifier, exposureData);
        return true;
    }
}
