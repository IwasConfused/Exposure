package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.world.camera.Camera;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * Injected in Entities such as {@link net.minecraft.world.entity.player.Player}. <br>
 * Injected interfaces must have all methods as 'default'.
 */
public interface CameraOperator {
    default Optional<Camera> getActiveExposureCamera() {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void setActiveExposureCamera(Camera camera) {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void removeActiveExposureCamera() {
        throw new IllegalStateException("This method must be implemented.");
    }

    // --

    default LivingEntity toEntity() {
        return (LivingEntity) this;
    }
}
