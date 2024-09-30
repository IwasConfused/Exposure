package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ClearRenderingCacheS2CP implements IPacket {
    public static final ClearRenderingCacheS2CP INSTANCE = new ClearRenderingCacheS2CP();

    public static final ResourceLocation ID = Exposure.resource("clear_rendering_cache");
    public static final CustomPacketPayload.Type<ClearRenderingCacheS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<ByteBuf, ClearRenderingCacheS2CP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ClearRenderingCacheS2CP() { }

    @Override

    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.clearRenderingCache();
        return true;
    }
}