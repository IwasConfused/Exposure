package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.item.CameraItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface PhotographerEntity {
    StreamCodec<ByteBuf, PhotographerEntity> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(
            PhotographerEntity::fromUUID, entity -> entity.asEntity().getUUID()
    );

    static PhotographerEntity fromUUID(UUID uuid) {
        // 1. This can cause problems potentially.
        // 2. Comparing against 'SERVER' because network client thread group is named 'main' not 'CLIENT'
        //    but server thread does have proper group name.
        if (Thread.currentThread().getThreadGroup().getName().equals("SERVER")) {
            for (ServerLevel level : PlatformHelper.getServer().getAllLevels()) {
                @Nullable Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    return ((PhotographerEntity) entity);
                }
            }
        } else {
            return ((PhotographerEntity) Minecrft.level().getEntities().get(uuid));
        }

        return null;
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
