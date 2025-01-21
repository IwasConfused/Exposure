package io.github.mortuusars.exposure.world.sound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.sound.OnePerEntitySoundsClient;
import io.github.mortuusars.exposure.world.camera.CameraID;
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
 * This system allows only one sound playing from a camera at a given time.
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

    public static void stop(@Nullable Player excludedPlayer, Entity entity, SoundEvent soundEvent) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            OnePerEntitySoundsClient.stop(entity, soundEvent);
            return;
        }

        Packets.sendToPlayersNear(new StopOnePerEntitySoundS2CP(entity.getId(), soundEvent),
                serverLevel, (ServerPlayer)excludedPlayer, entity.getX(), entity.getY(), entity.getZ(),
                soundEvent.getRange(1f) * 2);
    }

    public static void playForAllClients(Entity sourceEntity, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        if (!sourceEntity.level().isClientSide) {
            Packets.sendToAllClients(new PlayOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent, source, volume, pitch));
        }
    }

    public static void stopForAllClients(Entity entity, SoundEvent soundEvent) {
        if (!entity.level().isClientSide) {
            Packets.sendToAllClients(new StopOnePerEntitySoundS2CP(entity.getId(), soundEvent));
        }
    }

    public static void playShutterTickingSoundForAll(Entity entity, CameraID cameraID,
                                                     float volume, float pitch, int durationTicks) {
        if (!entity.level().isClientSide) {
            Packets.sendToAllClients(new PlayOnePerEntityShutterTickingSoundS2CP(entity.getId(), cameraID, volume, pitch, durationTicks));
        }
    }

    public static void stopShutterTickingSoundForAll(Entity entity) {
        if (!entity.level().isClientSide) {
            Packets.sendToAllClients(new StopOnePerEntitySoundS2CP(entity.getId(),
                    Exposure.SoundEvents.SHUTTER_TICKING.get()));
        }
    }
}
