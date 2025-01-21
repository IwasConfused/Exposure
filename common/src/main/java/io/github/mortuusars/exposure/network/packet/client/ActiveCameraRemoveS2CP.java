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

public class ActiveCameraRemoveS2CP implements IPacket {
    public static final ActiveCameraRemoveS2CP INSTANCE = new ActiveCameraRemoveS2CP();

    public static final ResourceLocation ID = Exposure.resource("active_camera_remove");
    public static final CustomPacketPayload.Type<ActiveCameraRemoveS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraRemoveS2CP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ActiveCameraRemoveS2CP() { }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        player.removeActiveExposureCamera();
        return true;
    }
}
