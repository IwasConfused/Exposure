package io.github.mortuusars.exposure.world.sound;

import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.UniqueSoundPlayShutterTickingS2CP;
import io.github.mortuusars.exposure.network.packet.client.UniqueSoundPlayS2CP;
import io.github.mortuusars.exposure.world.camera.CameraId;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class Sound {
    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource source) {
        play(level, x, y, z, sound ,source, 1F, 1F, 0F);
    }

    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
        play(level, x, y, z, sound ,source, volume, pitch, 0F);
    }

    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariety) {
        pitch = vary(pitch, pitchVariety);
        level.playSound(null, x, y, z, sound, source, volume, pitch);
    }

    public static void play(Entity entity, SoundEvent sound) {
        play(entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource source) {
        play(entity, sound, source, 1F, 1F, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        play(entity, sound, source, volume, pitch, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariety) {
        pitch = vary(pitch, pitchVariety);
        entity.level().playSound(null, entity, sound, source, volume, pitch);
    }

    // --

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource source) {
        playSided(player, x, y, z, sound, source, 1F, 1F, 0F);
    }

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
        playSided(player, x, y, z, sound, source, volume, pitch, 0F);
    }

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariety) {
        pitch = vary(pitch, pitchVariety);
        player.level().playSound(player, x, y, z, sound, source, volume, pitch);
    }

    public static void playSided(Player player, Entity entity, SoundEvent sound) {
        playSided(player, entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
    }

    public static void playSided(Player player, Entity entity, SoundEvent sound, SoundSource source) {
        playSided(player, entity, sound, source, 1F, 1F, 0F);
    }

    public static void playSided(Player player, Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        playSided(player, entity, sound, source, volume, pitch, 0F);
    }

    public static void playSided(Player player, Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariety) {
        pitch = vary(pitch, pitchVariety);
        player.level().playSound(player, entity, sound, source, volume, pitch);
    }

    public static void playSided(Entity entity, SoundEvent sound) {
        if (entity instanceof Player player) {
            playSided(player, entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
        } else {
            play(entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
        }
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource source) {
        if (entity instanceof Player player) {
            playSided(player, entity, sound, source, 1F, 1F, 0F);
        } else {
            play(entity, sound, source, 1F, 1F, 0F);
        }
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if (entity instanceof Player player) {
            playSided(player, entity, sound, source, volume, pitch, 0F);
        } else {
            play(entity, sound, source, volume, pitch, 0F);
        }
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariety) {
        pitch = vary(pitch, pitchVariety);
        @Nullable Player player = entity instanceof Player ? ((Player) entity) : null;
        entity.level().playSound(player, entity, sound, source, volume, pitch);
    }

    // --

    public static void playUnique(String id, Entity entity, SoundEvent sound, SoundSource source) {
        playUnique(id, entity, sound, source, 1F, 1F, 0F);
    }

    public static void playUnique(String id, Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        playUnique(id, entity, sound, source, volume, pitch, 0F);
    }

    public static void playUnique(String id, Entity entity, SoundEvent sound, SoundSource source,
                                  float volume, float pitch, float pitchVariety) {
        pitch = vary(pitch, pitchVariety);
        if (entity.level() instanceof ServerLevel serverLevel) {
            UniqueSoundPlayS2CP packet = new UniqueSoundPlayS2CP(id, entity.getId(), sound, source, volume, pitch);
            Packets.sendToPlayersNear(packet, serverLevel, null, entity, sound.getRange(1f) * 4);
        }
    }

    public static void playUniqueSided(String id, Player player, Entity entity, SoundEvent sound, SoundSource source,
                                       float volume, float pitch, float pitchVariety) {
        pitch = vary(pitch, pitchVariety);
        if (player.level() instanceof ServerLevel serverLevel) {
            UniqueSoundPlayS2CP packet = new UniqueSoundPlayS2CP(id, entity.getId(), sound, source, volume, pitch);
            Packets.sendToPlayersNear(packet, serverLevel, ((ServerPlayer) player), entity, sound.getRange(1f) * 4);
        } else if (player.level().isClientSide()) {
            UniqueSoundManager.play(id, entity, sound, source, volume, pitch);
        }
    }

    private static float vary(float value, float variety) {
        return value - (variety / 2) + ThreadLocalRandom.current().nextFloat() * variety;
    }

    public static void playShutterTicking(Entity entity, CameraId cameraId, int duration) {
        if (!entity.level().isClientSide()) {
            Packets.sendToAllClients(new UniqueSoundPlayShutterTickingS2CP(entity.getId(), cameraId, 1F, 1F, duration));
        }
    }
}
