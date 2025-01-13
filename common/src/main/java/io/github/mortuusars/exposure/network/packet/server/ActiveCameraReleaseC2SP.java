package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ActiveCameraReleaseC2SP(UUID photographerEntityID,
                                      CameraID cameraID) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("active_camera_release");
    public static final Type<ActiveCameraReleaseC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraReleaseC2SP> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ActiveCameraReleaseC2SP::photographerEntityID,
            CameraID.STREAM_CODEC, ActiveCameraReleaseC2SP::cameraID,
            ActiveCameraReleaseC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        PhotographerEntity.fromUUID(player.level(), photographerEntityID).ifPresentOrElse(photographer -> {
            photographer.getActiveExposureCamera().ifPresentOrElse(
                    Camera::release,
                    () -> Exposure.LOGGER.error("Cannot release shutter of an active camera: '{}' does not have an active camera.", photographer.asEntity()));
        }, () -> Exposure.LOGGER.error("Cannot release shutter of an active camera: PhotographerEntity is not found."));

        return true;
    }
}
