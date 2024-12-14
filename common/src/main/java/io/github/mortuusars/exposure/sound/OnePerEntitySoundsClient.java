package io.github.mortuusars.exposure.sound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.sound.instance.ShutterTimerTickingSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Because we are not using Holder of SoundEvent - SoundsEvents should be compared by their location, not directly.
 */
public class OnePerEntitySoundsClient {
    private static final Map<Entity, Map<ResourceLocation, SoundInstance>> sounds = new HashMap<>();

    public static void play(@NotNull Entity sourceEntity, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        SoundInstance soundInstance = createSoundInstance(sourceEntity, soundEvent, source, volume, pitch);

        Map<ResourceLocation, SoundInstance> instances = sounds.computeIfAbsent(sourceEntity, e -> new HashMap<>());

        @Nullable SoundInstance previousInstance = instances.remove(soundEvent.getLocation());
        if (previousInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(previousInstance);
        }

        instances.put(soundEvent.getLocation(), soundInstance);

        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    public static void stop(@NotNull Entity sourceEntity, SoundEvent soundEvent) {
        @Nullable Map<ResourceLocation, SoundInstance> instances = sounds.get(sourceEntity);
        if (instances == null) {
            return;
        }

        @Nullable SoundInstance instance = instances.remove(soundEvent.getLocation());
        if (instance != null) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
    }

    public static void playShutterTickingSound(CameraAccessor<?> cameraAccessor, Entity sourceEntity,
                                               float volume, float pitch, int durationTicks) {
        SoundEvent soundEvent = Exposure.SoundEvents.SHUTTER_TICKING.get();
        SoundInstance soundInstance = createShutterTickingSoundInstance(cameraAccessor, sourceEntity,
                soundEvent, volume, pitch, durationTicks);
        Map<ResourceLocation, SoundInstance> instances = sounds.computeIfAbsent(sourceEntity, e -> new HashMap<>());

        @Nullable SoundInstance previousInstance = instances.get(soundEvent.getLocation());
        if (previousInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(soundInstance);
            instances.remove(soundEvent.getLocation());
        }

        instances.put(soundEvent.getLocation(), soundInstance);

        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    public static void stopShutterTickingSound(@NotNull Entity sourceEntity) {
        stop(sourceEntity, Exposure.SoundEvents.SHUTTER_TICKING.get());
    }

    private static SoundInstance createSoundInstance(Entity sourceEntity, SoundEvent soundEvent, SoundSource source,
                                                     float volume, float pitch) {
        return new EntityBoundSoundInstance(soundEvent, source, volume, pitch, sourceEntity, sourceEntity.getRandom().nextLong());
    }

    private static SoundInstance createShutterTickingSoundInstance(CameraAccessor cameraAccessor, Entity sourceEntity,
                                                                   SoundEvent soundEvent, float volume, float pitch, int durationTicks) {
        return new ShutterTimerTickingSoundInstance(cameraAccessor, sourceEntity, soundEvent, SoundSource.PLAYERS,
                volume, pitch, durationTicks, sourceEntity.getRandom().nextLong());
    }
}
