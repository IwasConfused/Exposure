package io.github.mortuusars.exposure.mixin;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(Player.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class PlayerMixin extends LivingEntity implements CameraHolder, CameraOperator {
    @Unique
    @Nullable
    protected Camera activeExposureCamera;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    // --

    @Override
    public @NotNull Player getPlayerExecutingExposure() {
        return (Player) (Object) this;
    }

    @Override
    public Optional<Player> getPlayerAwardedForExposure() {
        return Optional.of((Player) (Object) this);
    }

    @Override
    public @NotNull Entity getExposureAuthorEntity() {
        return this;
    }

    // --

    @Override
    public Optional<Camera> getActiveExposureCamera() {
        return Optional.ofNullable(activeExposureCamera);
    }

    @Override
    public void setActiveExposureCamera(@NotNull Camera camera) {
        Preconditions.checkNotNull(camera, "null is not allowed here. Use 'removeActiveExposureCamera'.");
        activeExposureCamera = camera;
    }

    @Override
    public void removeActiveExposureCamera() {
        activeExposureCamera = null;
    }
}
