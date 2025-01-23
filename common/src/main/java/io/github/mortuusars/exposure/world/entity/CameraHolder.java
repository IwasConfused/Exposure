package io.github.mortuusars.exposure.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Injected in Entities such as {@link net.minecraft.world.entity.player.Player} and others in the future. <br>
 * Injected interfaces must have all methods as 'default'.
 */
public interface CameraHolder {
    /**
     * Player that captures the image (renders it).
     */
    default @NotNull Player getPlayerExecutingExposure() {
        throw new IllegalStateException("This method must be implemented, " +
                "and should return a player that will render the image.");
    }

    /**
     * Used to trigger advancements and award stats.
     */
    default Optional<Player> getPlayerAwardedForExposure() {
        throw new IllegalStateException("This method must be implemented, " +
                "and should return a player that will receive advancements or stats for exposure (if applicable).");
    }

    /**
     * Entity that will be treated as an author of a photo.
     */
    default @NotNull Entity getExposureAuthorEntity() {
        throw new IllegalStateException("This method must be implemented, " +
                "and should return an entity that will be treated as an author of a photo.");
    }

    default Optional<CameraOperator> getExposureCameraOperator() {
        throw new IllegalStateException("This method must be implemented, and should return an operator (if present).");
    }

    // --

    default Entity asEntity() {
        return ((Entity) this);
    }
}
