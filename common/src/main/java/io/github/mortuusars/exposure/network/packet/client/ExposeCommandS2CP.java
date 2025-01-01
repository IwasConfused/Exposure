package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ExposeCommandS2CP(String exposureId, int size, float brightnessStops) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("expose_command");
    public static final CustomPacketPayload.Type<ExposeCommandS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExposeCommandS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ExposeCommandS2CP::exposureId,
            ByteBufCodecs.VAR_INT, ExposeCommandS2CP::size,
            ByteBufCodecs.FLOAT, ExposeCommandS2CP::brightnessStops,
            ExposeCommandS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.exposeScreenshot(exposureId, size, brightnessStops);
        return true;
    }
}
