package io.github.mortuusars.exposure.sound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.PlayOnePerEntityShutterTickingSoundS2CP;
import io.github.mortuusars.exposure.network.packet.client.PlayOnePerEntitySoundS2CP;
import io.github.mortuusars.exposure.network.packet.client.StopOnePerEntitySoundS2CP;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This system is made to allow only one sound playing from a camera at a given time.
 * Otherwise, when rapidly photographing, sounds (like film advancing) will play overlapping each other,
 * which doesn't sound good (and doesn't make sense either).
 */
public class OnePerEntitySounds {
    public static void play(@Nullable Player excludedPlayer, @NotNull Entity sourceEntity, SoundEvent soundEvent,
                            SoundSource source, float volume, float pitch) {
        if (!(sourceEntity.level() instanceof ServerLevel serverLevel)) {
            OnePerEntitySoundsClient.play(sourceEntity, soundEvent, source, volume, pitch);
            return;
        }

        Packets.sendToPlayersNear(new PlayOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent, source, volume, pitch),
                serverLevel, (ServerPlayer)excludedPlayer, sourceEntity.getX(), sourceEntity.getY(), sourceEntity.getZ(),
                soundEvent.getRange(1f) * 2);
    }

    public static void stop(@Nullable Player excludedPlayer, Entity sourceEntity, SoundEvent soundEvent) {
        if (!(sourceEntity.level() instanceof ServerLevel serverLevel)) {
            OnePerEntitySoundsClient.stop(sourceEntity, soundEvent);
            return;
        }

        Packets.sendToPlayersNear(new StopOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent),
                serverLevel, (ServerPlayer)excludedPlayer, sourceEntity.getX(), sourceEntity.getY(), sourceEntity.getZ(),
                soundEvent.getRange(1f) * 2);
    }

    public static void playForAllClients(Entity sourceEntity, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        if (!sourceEntity.level().isClientSide) {
            Packets.sendToAllClients(new PlayOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent, source, volume, pitch));
        }
    }

    public static void stopForAllClients(Entity sourceEntity, SoundEvent soundEvent) {
        if (!sourceEntity.level().isClientSide) {
            Packets.sendToAllClients(new StopOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent));
        }
    }

    public static void playShutterTickingSoundForAllPlayers(CameraAccessor cameraAccessor, Entity sourceEntity,
                                                            float volume, float pitch, int durationTicks) {
        if (!sourceEntity.level().isClientSide) {
            Packets.sendToAllClients(new PlayOnePerEntityShutterTickingSoundS2CP(cameraAccessor,
                    sourceEntity.getUUID(), volume, pitch, durationTicks));
        }
    }

    public static void stopShutterTickingSoundForAllPlayers(Entity sourceEntity) {
        if (!sourceEntity.level().isClientSide) {
            Packets.sendToAllClients(new StopOnePerEntitySoundS2CP(sourceEntity.getUUID(), Exposure.SoundEvents.SHUTTER_TICKING.get()));
        }
    }
}
