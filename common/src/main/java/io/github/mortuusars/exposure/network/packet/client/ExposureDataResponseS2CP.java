package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.foundation.warehouse.RequestedExposureData;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

//TODO: comment about not needing packet splitting, if it works without it
public record ExposureDataResponseS2CP(ExposureIdentifier identifier, RequestedExposureData result) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_response");
    public static final Type<ExposureDataResponseS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExposureDataResponseS2CP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, ExposureDataResponseS2CP::identifier,
            RequestedExposureData.STREAM_CODEC, ExposureDataResponseS2CP::result,
            ExposureDataResponseS2CP::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureClient.exposureStore().receive(identifier, result);
        return true;
    }
}
