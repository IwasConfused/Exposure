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

public record LoadExposureFromFileCommandS2CP(String id, String filePath, int size, boolean dither) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("load_exposure_from_file");
    public static final CustomPacketPayload.Type<LoadExposureFromFileCommandS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, LoadExposureFromFileCommandS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, LoadExposureFromFileCommandS2CP::id,
            ByteBufCodecs.STRING_UTF8, LoadExposureFromFileCommandS2CP::filePath,
            ByteBufCodecs.VAR_INT, LoadExposureFromFileCommandS2CP::size,
            ByteBufCodecs.BOOL, LoadExposureFromFileCommandS2CP::dither,
            LoadExposureFromFileCommandS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.loadExposure(id, filePath, size, dither);
        return true;
    }
}
