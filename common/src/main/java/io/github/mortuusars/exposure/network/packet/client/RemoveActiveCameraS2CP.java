package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record RemoveActiveCameraS2CP() implements IPacket {
    private static final RemoveActiveCameraS2CP INSTANCE = new RemoveActiveCameraS2CP();

    public static final ResourceLocation ID = Exposure.resource("remove_active_camera");
    public static final Type<RemoveActiveCameraS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RemoveActiveCameraS2CP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        player.removeActiveCamera();
        return true;
    }
}
