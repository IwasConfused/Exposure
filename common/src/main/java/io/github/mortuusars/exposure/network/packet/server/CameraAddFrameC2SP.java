package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.core.ExposureFrameDataFromClient;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record CameraAddFrameC2SP(CameraAccessor cameraAccessor,
                                 ExposureFrameDataFromClient frameDataFromClient) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("camera_add_frame");
    public static final CustomPacketPayload.Type<CameraAddFrameC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CameraAddFrameC2SP> STREAM_CODEC = StreamCodec.composite(
            CameraAccessor.STREAM_CODEC, CameraAddFrameC2SP::cameraAccessor,
            ExposureFrameDataFromClient.STREAM_CODEC, CameraAddFrameC2SP::frameDataFromClient,
            CameraAddFrameC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");
        ServerPlayer serverPlayer = ((ServerPlayer) player);

        cameraAccessor.getCamera(player).ifPresent(camera -> {
            ExposureFrame frame = camera.getItem().createExposureFrame(serverPlayer, camera.getItemStack(), frameDataFromClient);
            camera.getItem().addFrame(serverPlayer, camera.getItemStack(), frame);
        });

        return true;
    }
}
