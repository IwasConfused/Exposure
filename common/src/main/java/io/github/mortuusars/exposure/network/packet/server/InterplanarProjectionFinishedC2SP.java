package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.server.CameraInstances;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record InterplanarProjectionFinishedC2SP(UUID photographerID,
                                                UUID cameraID,
                                                boolean isSuccessful) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("interplanar_projector_finished");
    public static final Type<InterplanarProjectionFinishedC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, InterplanarProjectionFinishedC2SP> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, InterplanarProjectionFinishedC2SP::photographerID,
            UUIDUtil.STREAM_CODEC, InterplanarProjectionFinishedC2SP::cameraID,
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
            @Nullable Entity photographer = serverPlayer.serverLevel().getEntity(photographerID);
            if (photographer == null) {
                Exposure.LOGGER.error("Cannot handle '{}' packet: photographer entity with UUID '{}' is not found.", ID, photographerID);
                return true;
            }

            photographer.level().playSound(player, photographer, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(),
                    SoundSource.PLAYERS, 0.8f, 1.1f);

            serverPlayer.serverLevel().sendParticles(ParticleTypes.PORTAL, photographer.getX(),
                    photographer.getY() + 1.2,
                    photographer.getZ(),
                    32,
                    photographer.getRandom().nextGaussian(),
                    photographer.getRandom().nextGaussian(),
                    photographer.getRandom().nextGaussian(), 0.01);

            CameraInstances.ifPresent(cameraID, cameraInstance -> {
                cameraInstance.setProjectionResult(isSuccessful);
            });
        }

        return true;
    }
}
