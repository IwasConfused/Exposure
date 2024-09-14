package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ApplyShaderS2CP(ResourceLocation shaderLocation) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("apply_shader");
    public static final CustomPacketPayload.Type<ApplyShaderS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ApplyShaderS2CP> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ApplyShaderS2CP::shaderLocation,
            ApplyShaderS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.applyShader(this);
        return true;
    }
}
