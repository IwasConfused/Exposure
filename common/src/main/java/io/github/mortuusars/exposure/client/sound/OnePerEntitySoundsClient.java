package io.github.mortuusars.exposure.client.sound;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.client.sound.instance.ShutterTimerTickingSoundInstance;
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
    //TODO: use multimap
    private static final Map<Entity, Map<ResourceLocation, SoundInstance>> SOUNDS = new HashMap<>();

    public static void play(@NotNull Entity sourceEntity, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        SoundInstance soundInstance = createSoundInstance(sourceEntity, soundEvent, source, volume, pitch);

        Map<ResourceLocation, SoundInstance> instances = SOUNDS.computeIfAbsent(sourceEntity, e -> new HashMap<>());

        @Nullable SoundInstance previousInstance = instances.remove(soundEvent.getLocation());
        if (previousInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(previousInstance);
        }

//        Table<String, ResourceLocation, SoundInstance> sounds = HashBasedTable.create();
//        @Nullable SoundInstance instance = sounds.get("asd", soundEvent.getLocation());

        instances.put(soundEvent.getLocation(), soundInstance);

        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    public static void stop(@NotNull Entity sourceEntity, SoundEvent soundEvent) {
        @Nullable Map<ResourceLocation, SoundInstance> instances = SOUNDS.get(sourceEntity);
        if (instances == null) {
            return;
        }

        @Nullable SoundInstance instance = instances.remove(soundEvent.getLocation());
        if (instance != null) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
    }

    public static void playShutterTicking(Entity entity, CameraID cameraID,
                                          float volume, float pitch, int durationTicks) {
        SoundEvent soundEvent = Exposure.SoundEvents.SHUTTER_TICKING.get();
        SoundInstance soundInstance = createShutterTickingSoundInstance(entity, cameraID, soundEvent, volume, pitch, durationTicks);
        Map<ResourceLocation, SoundInstance> instances = SOUNDS.computeIfAbsent(entity, e -> new HashMap<>());

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

    private static SoundInstance createShutterTickingSoundInstance(Entity entity, CameraID cameraID,
                                                                   SoundEvent soundEvent, float volume, float pitch, int durationTicks) {
        return new ShutterTimerTickingSoundInstance(entity, cameraID, soundEvent, SoundSource.PLAYERS, volume, pitch, durationTicks);
    }
}
