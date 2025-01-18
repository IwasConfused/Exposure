package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.CameraItem;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface PhotographerEntity {
    static Optional<PhotographerEntity> fromUuid(Level level, UUID uuid) {
        if (uuid.equals(Util.NIL_UUID)) return Optional.empty();
        return level.getEntities().get(uuid) instanceof PhotographerEntity photographerEntity
                ? Optional.of(photographerEntity)
                : Optional.empty();
    }

    static Optional<PhotographerEntity> fromUuidOrWrap(Level level, UUID uuid, Player executingPlayer) {
        return fromUuid(level, uuid).or(() -> {
            @Nullable Entity entity = level.getEntities().get(uuid);
            if (entity == null) return Optional.empty();
            return Optional.of(new WrappedPhotographerEntity(entity, executingPlayer));
        });
    }

    /**
     * Player that captures the image (renders it).
     */
    default @NotNull Player getExecutingPlayer() {
        throw new IllegalStateException("This method must be overriden, and should return a player that will render the image.");
    }

    /**
     * Used to trigger advancements and award stats.
     */
    default @Nullable Player getOwnerPlayer() {
        return null;
    }

    /**
     * Used to add photographer data to frame.
     */
    default Entity getOwnerEntity() {
        throw new IllegalStateException("This method must be overriden, and should return an entity that will show up as an author of a frame.");
    }

    default Entity asEntity() {
        return ((Entity) this);
    }

    default @Nullable Camera activeExposureCamera() {
        return null;
    }

    default Optional<Camera> getActiveExposureCamera() {
        return Optional.ofNullable(activeExposureCamera());
    }

    default void ifActiveExposureCameraPresent(BiConsumer<CameraItem, ItemStack> ifPresent) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null) {
            camera.ifPresent(ifPresent);
        }
    }

    default void ifActiveExposureCameraPresent(BiConsumer<CameraItem, ItemStack> ifPresent, Runnable orElse) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null) {
            camera.ifPresent(ifPresent);
        } else {
            orElse.run();
        }
    }

    default <T> T mapActiveExposureCamera(BiFunction<CameraItem, ItemStack, T> map, T orElse) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null) {
            return camera.map(map, orElse);
        } else {
            return orElse;
        }
    }

    default void mapActiveExposureCamera(BiConsumer<CameraItem, ItemStack> ifPresent, Runnable orElse) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null) {
            camera.ifPresent(ifPresent);
        } else {
            orElse.run();
        }
    }

    default void setActiveExposureCamera(Camera camera) {
    }

    default void removeActiveExposureCamera() {
    }

    default boolean activeExposureCameraMatches(ItemStack stack) {
        return mapActiveExposureCamera((item, cStack) -> cStack.equals(stack), false);
    }

    default void playCameraSoundSided(SoundEvent sound, float volume, float pitch, float pitchVariety) {
        Entity entity = asEntity();
        @Nullable Player excludedPlayer = entity instanceof Player player ? player : null;

        if (pitchVariety > 0f) {
            pitch = pitch - (pitchVariety / 2f) + (entity.getRandom().nextFloat() * pitchVariety);
        }

        entity.level().playSound(excludedPlayer, entity, sound, SoundSource.PLAYERS, volume, pitch);
    }

    default void playCameraSound(SoundEvent sound, float volume, float pitch, float pitchVariety) {
        Entity entity = asEntity();

        if (pitchVariety > 0f) {
            pitch = pitch - (pitchVariety / 2f) + (entity.getRandom().nextFloat() * pitchVariety);
        }

        entity.level().playSound(null, entity, sound, SoundSource.PLAYERS, volume, pitch);
    }
}
