package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record OnFrameAddedS2CP(ExposureFrame frame) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("on_frame_added");
    public static final CustomPacketPayload.Type<OnFrameAddedS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, OnFrameAddedS2CP> STREAM_CODEC = StreamCodec.composite(
            ExposureFrame.STREAM_CODEC, OnFrameAddedS2CP::frame,
            OnFrameAddedS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.onFrameAdded(this);
        return true;
    }
}
