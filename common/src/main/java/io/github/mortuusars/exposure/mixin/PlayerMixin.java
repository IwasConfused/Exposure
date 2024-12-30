package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class PlayerMixin extends LivingEntity implements PhotographerEntity {
    @Shadow public abstract void resetAttackStrengthTicker();

    @Unique
    private Camera activeExposureCamera;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @NotNull Player getExecutingPlayer() {
        return (Player) (Object) this;
    }

    @Override
    public @Nullable Player getOwnerPlayer() {
        return (Player) (Object) this;
    }

    @Override
    public Entity getOwnerEntity() {
        return getExecutingPlayer();
    }

    @Override
    public @Nullable Camera activeExposureCamera() {
        return activeExposureCamera;
    }

    @Override
    public void setActiveExposureCamera(Camera camera) {
        activeExposureCamera = camera;
    }

    @Override
    public void removeActiveExposureCamera() {
        activeExposureCamera = null;
    }
}
