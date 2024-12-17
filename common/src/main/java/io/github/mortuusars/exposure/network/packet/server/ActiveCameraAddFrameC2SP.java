package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.ExposureFrameClientData;
import io.github.mortuusars.exposure.core.camera.ActiveCameraHolder;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ActiveCameraAddFrameC2SP(UUID cameraHolderID,
                                       ExposureFrameClientData frameDataFromClient) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("active_camera_add_frame");
    public static final Type<ActiveCameraAddFrameC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraAddFrameC2SP> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ActiveCameraAddFrameC2SP::cameraHolderID,
            ExposureFrameClientData.STREAM_CODEC, ActiveCameraAddFrameC2SP::frameDataFromClient,
            ActiveCameraAddFrameC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ServerPlayer serverPlayer = ((ServerPlayer) player);

        Entity entity = serverPlayer.serverLevel().getEntity(cameraHolderID);

        if (!(entity instanceof ActiveCameraHolder cameraHolder)) {
            Exposure.LOGGER.error("Cannot handle '{}' packet: Entity '{}' is not ActiveCameraHolder", ID, entity);
            return true;
        }

        serverPlayer.server.execute(() -> {
            cameraHolder.getActiveCamera().ifPresentOrElse(camera -> {
                ExposureFrame frame = camera.getItem().createExposureFrame(serverPlayer.serverLevel(),
                        serverPlayer, camera.getItemStack(), frameDataFromClient);
                camera.getItem().addFrame(serverPlayer, camera.getItemStack(), frame);
            }, () -> Exposure.LOGGER.error("Cannot create and add ExposureFrame: Entity '{}' does not have an active camera.", entity));
        });

        return true;
    }
}
