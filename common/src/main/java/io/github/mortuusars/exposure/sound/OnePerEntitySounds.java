package io.github.mortuusars.exposure.sound;

import io.github.mortuusars.exposure.network.Packets;
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

public class OnePerEntitySounds {
    public static void play(@Nullable Player player, @NotNull Entity sourceEntity, SoundEvent soundEvent,
                            SoundSource source, float volume, float pitch) {
        if (!(sourceEntity.level() instanceof ServerLevel serverLevel)) {
            OnePerEntitySoundsClient.play(sourceEntity, soundEvent, source, volume, pitch);
            return;
        }

        Packets.sendToPlayersNear(new PlayOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent, source, volume, pitch),
                serverLevel, (ServerPlayer)player, sourceEntity.getX(), sourceEntity.getY(), sourceEntity.getZ(),
                soundEvent.getRange(1f) * 2);
    }

    public static void stop(@Nullable Player player, Entity sourceEntity, SoundEvent soundEvent) {
        if (!(sourceEntity.level() instanceof ServerLevel serverLevel)) {
            OnePerEntitySoundsClient.stop(sourceEntity, soundEvent);
            return;
        }

        Packets.sendToPlayersNear(new StopOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent),
                serverLevel, (ServerPlayer)player, sourceEntity.getX(), sourceEntity.getY(), sourceEntity.getZ(),
                soundEvent.getRange(1f) * 2);
    }

    public static void playForAllClients(Entity sourceEntity, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        if (!sourceEntity.level().isClientSide)
            Packets.sendToAllClients(new PlayOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent, source, volume, pitch));
    }

    public static void stopForAllClients(Entity sourceEntity, SoundEvent soundEvent) {
        if (!sourceEntity.level().isClientSide)
            Packets.sendToAllClients(new StopOnePerEntitySoundS2CP(sourceEntity.getUUID(), soundEvent));
    }
}
