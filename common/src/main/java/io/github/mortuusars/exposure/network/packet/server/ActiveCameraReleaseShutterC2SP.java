package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ActiveCameraReleaseShutterC2SP implements IPacket {
    public static final ActiveCameraReleaseShutterC2SP INSTANCE = new ActiveCameraReleaseShutterC2SP();
    public static final Type<ActiveCameraReleaseShutterC2SP> TYPE = new Type<>(Exposure.resource("active_camera_release_shutter"));
    public static final StreamCodec<FriendlyByteBuf, ActiveCameraReleaseShutterC2SP> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    private ActiveCameraReleaseShutterC2SP() {
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        player.getActiveExposureCamera().ifPresentOrElse(
                Camera::release,
                () -> Exposure.LOGGER.error("Cannot release shutter: '{}' does not have an active camera.", player));

        return true;
    }
}
