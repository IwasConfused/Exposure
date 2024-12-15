package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SetActiveInHandCameraS2CP(UUID ownerUUID, InteractionHand hand) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("set_active_camera");
    public static final Type<SetActiveInHandCameraS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SetActiveInHandCameraS2CP> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SetActiveInHandCameraS2CP::ownerUUID,
            ByteBufCodecs.idMapper(id -> InteractionHand.values()[id], InteractionHand::ordinal), SetActiveInHandCameraS2CP::hand,
            SetActiveInHandCameraS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.setInHandActiveCamera(this);
        return true;
    }
}
