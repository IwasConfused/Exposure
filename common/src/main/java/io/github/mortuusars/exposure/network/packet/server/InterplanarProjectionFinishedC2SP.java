package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.server.CameraInstances;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record InterplanarProjectionFinishedC2SP(PhotographerEntity photographer,
                                                CameraID cameraID,
                                                boolean isSuccessful) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("interplanar_projector_finished");
    public static final Type<InterplanarProjectionFinishedC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, InterplanarProjectionFinishedC2SP> STREAM_CODEC = StreamCodec.composite(
            PhotographerEntity.STREAM_CODEC, InterplanarProjectionFinishedC2SP::photographer,
            CameraID.STREAM_CODEC, InterplanarProjectionFinishedC2SP::cameraID,
            ByteBufCodecs.BOOL, InterplanarProjectionFinishedC2SP::isSuccessful,
            InterplanarProjectionFinishedC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            photographer.playCameraSound(Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), 0.8f, 1.1f, 0f);

            Entity entity = photographer.asEntity();

            serverPlayer.serverLevel().sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1.2, entity.getZ(), 32,
                    entity.getRandom().nextGaussian(), entity.getRandom().nextGaussian(), entity.getRandom().nextGaussian(), 0.01);

            CameraInstances.ifPresent(cameraID, cameraInstance -> cameraInstance.setProjectionResult(isSuccessful));
        }

        return true;
    }
}
