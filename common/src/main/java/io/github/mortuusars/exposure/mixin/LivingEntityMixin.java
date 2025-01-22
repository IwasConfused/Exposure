package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getMaxHeadRotationRelativeToBody", at = @At("RETURN"), cancellable = true)
    private void getMaxHeadRotation(CallbackInfoReturnable<Float> cir) {
        if (this instanceof CameraOperator operator && operator.getActiveExposureCamera() != null) {
            cir.setReturnValue(Math.min(cir.getReturnValue(), 30));
        }
    }
}
