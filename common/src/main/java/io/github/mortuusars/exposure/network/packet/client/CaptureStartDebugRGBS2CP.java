package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CaptureStartDebugRGBS2CP(ResourceLocation templateId, List<CaptureProperties> captureProperties) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("capture_start_debug_rgb");
    public static final Type<CaptureStartDebugRGBS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CaptureStartDebugRGBS2CP> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CaptureStartDebugRGBS2CP::templateId,
            CaptureProperties.STREAM_CODEC.apply(ByteBufCodecs.list(3)), CaptureStartDebugRGBS2CP::captureProperties,
            CaptureStartDebugRGBS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.startDebugRGBCapture(this);
        return true;
    }
}
