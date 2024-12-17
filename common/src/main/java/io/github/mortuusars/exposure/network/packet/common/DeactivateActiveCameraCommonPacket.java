package io.github.mortuusars.exposure.network.packet.common;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class DeactivateActiveCameraCommonPacket implements IPacket {
    public static final DeactivateActiveCameraCommonPacket INSTANCE = new DeactivateActiveCameraCommonPacket();

    public static final ResourceLocation ID = Exposure.resource("deactivate_active_camera");
    public static final Type<DeactivateActiveCameraCommonPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, DeactivateActiveCameraCommonPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private DeactivateActiveCameraCommonPacket() {
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        player.ifActiveExposureCameraPresent((item, stack) -> item.deactivate(player, stack),
                () -> Exposure.LOGGER.error("Cannot deactivate a camera: player '{}' does not have an active camera.",
                        player.getScoreboardName()));
        return true;
    }
}
