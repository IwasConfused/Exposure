package io.github.mortuusars.exposure.world.sound;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class Sound {
    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource category) {
        play(level, x, y, z, sound ,category, 1F, 1F, 0F);
    }

    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        play(level, x, y, z, sound ,category, volume, pitch, 0F);
    }

    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, float pitchVariety) {
        pitch = pitch - (pitchVariety / 2) + ThreadLocalRandom.current().nextFloat() * pitchVariety;
        level.playSound(null, x, y, z, sound, category, volume, pitch);
    }

    public static void play(Entity entity, SoundEvent sound) {
        play(entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource category) {
        play(entity, sound, category, 1F, 1F, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        play(entity, sound, category, volume, pitch, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch, float pitchVariety) {
        pitch = pitch - (pitchVariety / 2) + ThreadLocalRandom.current().nextFloat() * pitchVariety;
        entity.level().playSound(null, entity, sound, category, volume, pitch);
    }

    // --

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource category) {
        playSided(player, x, y, z, sound, category, 1F, 1F, 0F);
    }

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        playSided(player, x, y, z, sound, category, volume, pitch, 0F);
    }

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, float pitchVariety) {
        pitch = pitch - (pitchVariety / 2) + ThreadLocalRandom.current().nextFloat() * pitchVariety;
        player.level().playSound(player, x, y, z, sound, category, volume, pitch);
    }

    public static void playSided(Player player, Entity entity, SoundEvent sound) {
        playSided(player, entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
    }

    public static void playSided(Player player, Entity entity, SoundEvent sound, SoundSource category) {
        playSided(player, entity, sound, category, 1F, 1F, 0F);
    }

    public static void playSided(Player player, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        playSided(player, entity, sound, category, volume, pitch, 0F);
    }

    public static void playSided(Player player, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch, float pitchVariety) {
        pitch = pitch - (pitchVariety / 2) + ThreadLocalRandom.current().nextFloat() * pitchVariety;
        player.level().playSound(player, entity, sound, category, volume, pitch);
    }

    public static void playSided(Entity entity, SoundEvent sound) {
        if (entity instanceof Player player) {
            playSided(player, entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
        } else {
            play(entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
        }
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource category) {
        if (entity instanceof Player player) {
            playSided(player, entity, sound, category, 1F, 1F, 0F);
        } else {
            play(entity, sound, category, 1F, 1F, 0F);
        }
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        if (entity instanceof Player player) {
            playSided(player, entity, sound, category, volume, pitch, 0F);
        } else {
            play(entity, sound, category, volume, pitch, 0F);
        }
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch, float pitchVariety) {
        pitch = pitch - (pitchVariety / 2) + ThreadLocalRandom.current().nextFloat() * pitchVariety;
        @Nullable Player player = entity instanceof Player ? ((Player) entity) : null;
        entity.level().playSound(player, entity, sound, category, volume, pitch);
    }
}
