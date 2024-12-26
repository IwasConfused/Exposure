package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.warehouse.ExposureClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ExposureClientDataC2SP(ExposureIdentifier identifier, ExposureClientData clientData) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("exposure_client_data");
    public static final Type<ExposureClientDataC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExposureClientDataC2SP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, ExposureClientDataC2SP::identifier,
            ExposureClientData.STREAM_CODEC, ExposureClientDataC2SP::clientData,
            ExposureClientDataC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureServer.vault().handleClientUpload(((ServerPlayer) player), identifier, clientData);
        return true;
    }
}
