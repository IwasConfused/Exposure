package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ExposureRequestC2SP(ExposureIdentifier identifier) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("exposure_request");
    public static final Type<ExposureRequestC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExposureRequestC2SP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, ExposureRequestC2SP::identifier,
            ExposureRequestC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureServer.vault().handleClientRequest((ServerPlayer) player, identifier);
        return true;
    }
}
