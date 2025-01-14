package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
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

public record LoadExposureCommandS2CP(String id, String path, int size, boolean dither) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("load_exposure");
    public static final CustomPacketPayload.Type<LoadExposureCommandS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, LoadExposureCommandS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, LoadExposureCommandS2CP::id,
            ByteBufCodecs.STRING_UTF8, LoadExposureCommandS2CP::path,
            ByteBufCodecs.VAR_INT, LoadExposureCommandS2CP::size,
            ByteBufCodecs.BOOL, LoadExposureCommandS2CP::dither,
            LoadExposureCommandS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.loadExposure(id, path, size, dither);
        return true;
    }
}
