package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.TranslatableError;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record InterplanarProjectionFinishedC2SP(CameraID cameraID,
                                                boolean successful,
                                                Optional<TranslatableError> error) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("interplanar_projection_finished");
    public static final Type<InterplanarProjectionFinishedC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, InterplanarProjectionFinishedC2SP> STREAM_CODEC = StreamCodec.composite(
            CameraID.STREAM_CODEC, InterplanarProjectionFinishedC2SP::cameraID,
            ByteBufCodecs.BOOL, InterplanarProjectionFinishedC2SP::successful,
            ByteBufCodecs.optional(TranslatableError.STREAM_CODEC), InterplanarProjectionFinishedC2SP::error,
            InterplanarProjectionFinishedC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        CameraInstances.ifPresent(cameraID, cameraInstance -> cameraInstance.setProjectionResult(player.level(), successful, error));
        return true;
    }
}
