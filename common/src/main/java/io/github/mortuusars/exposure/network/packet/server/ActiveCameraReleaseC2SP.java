package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.CaptureClientData;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ActiveCameraReleaseC2SP(PhotographerEntity photographer,
                                      CameraID cameraID) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("active_camera_release");
    public static final Type<ActiveCameraReleaseC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraReleaseC2SP> STREAM_CODEC = StreamCodec.composite(
            PhotographerEntity.STREAM_CODEC, ActiveCameraReleaseC2SP::photographer,
            CameraID.STREAM_CODEC, ActiveCameraReleaseC2SP::cameraID,
            ActiveCameraReleaseC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ServerLevel level = ((ServerPlayer) player).serverLevel();

        level.getServer().execute(() -> {
            photographer.getActiveExposureCamera().ifPresentOrElse(
                    Camera::release,
                    () -> Exposure.LOGGER.error("Cannot release: '{}' does not have an active camera.", photographer.asEntity()));
        });

        return true;
    }
}
