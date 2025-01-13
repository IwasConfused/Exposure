package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.TranslatableError;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record InterplanarProjectionFinishedC2SP(UUID photographerEntityID,
                                                CameraID cameraID,
                                                boolean isSuccessful,
                                                Optional<TranslatableError> error) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("interplanar_projector_finished");
    public static final Type<InterplanarProjectionFinishedC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, InterplanarProjectionFinishedC2SP> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, InterplanarProjectionFinishedC2SP::photographerEntityID,
            CameraID.STREAM_CODEC, InterplanarProjectionFinishedC2SP::cameraID,
            ByteBufCodecs.BOOL, InterplanarProjectionFinishedC2SP::isSuccessful,
            ByteBufCodecs.optional(TranslatableError.STREAM_CODEC), InterplanarProjectionFinishedC2SP::error,
            InterplanarProjectionFinishedC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        CameraInstances.ifPresent(cameraID, cameraInstance -> cameraInstance.setProjectionResult(player.level(), isSuccessful, error));
        return true;
    }
}
