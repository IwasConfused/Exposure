package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.ExposureFrameClientData;
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

public record ActiveCameraAddFrameC2SP(PhotographerEntity photographer,
                                       ExposureFrameClientData frameDataFromClient) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("active_camera_add_frame");
    public static final Type<ActiveCameraAddFrameC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraAddFrameC2SP> STREAM_CODEC = StreamCodec.composite(
            PhotographerEntity.STREAM_CODEC, ActiveCameraAddFrameC2SP::photographer,
            ExposureFrameClientData.STREAM_CODEC, ActiveCameraAddFrameC2SP::frameDataFromClient,
            ActiveCameraAddFrameC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ServerLevel level = ((ServerPlayer) player).serverLevel();

        level.getServer().execute(() -> photographer.ifActiveExposureCameraPresent(
                (item, stack) -> item.addNewFrame(photographer, level, stack, frameDataFromClient),
                () -> Exposure.LOGGER.error("Cannot create and add ExposureFrame: " +
                        "Photographer '{}' does not have an active camera.", photographer)));

        return true;
    }
}
