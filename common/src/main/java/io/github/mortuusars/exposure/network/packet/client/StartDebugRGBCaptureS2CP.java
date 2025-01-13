package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record StartDebugRGBCaptureS2CP(ResourceLocation templateId, List<CaptureProperties> captureProperties) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("start_debug_rgb_capture");
    public static final Type<StartDebugRGBCaptureS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, StartDebugRGBCaptureS2CP> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, StartDebugRGBCaptureS2CP::templateId,
            CaptureProperties.STREAM_CODEC.apply(ByteBufCodecs.list(3)), StartDebugRGBCaptureS2CP::captureProperties,
            StartDebugRGBCaptureS2CP::new
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
