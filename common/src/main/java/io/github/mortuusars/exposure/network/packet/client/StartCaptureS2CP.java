package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.frame.CaptureProperties;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record StartCaptureS2CP(CaptureProperties captureProperties) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("start_capture");
    public static final Type<StartCaptureS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, StartCaptureS2CP> STREAM_CODEC = StreamCodec.composite(
            CaptureProperties.STREAM_CODEC, StartCaptureS2CP::captureProperties,
            StartCaptureS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.startCapture(this);
        return true;
    }
}
