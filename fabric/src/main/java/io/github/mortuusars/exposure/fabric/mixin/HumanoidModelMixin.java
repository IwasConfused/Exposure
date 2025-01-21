package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.client.animation.ModelPoses;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    void onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player))
            return;

        if (player.getActiveExposureCamera().orElse(null) instanceof CameraInHand camera) {
            HumanoidArm arm = camera.getHand() == InteractionHand.MAIN_HAND
                    ? Minecraft.getInstance().options.mainHand().get()
                    : Minecraft.getInstance().options.mainHand().get().getOpposite();

            camera.ifPresent((item, stack) -> {
                if (item.isInSelfieMode(stack)) {
                    ModelPoses.applyCameraSelfiePose((HumanoidModel<?>) (Object) this, entity, arm, false);
                } else {
                    ModelPoses.applyCameraPose(((HumanoidModel<?>) (Object) this), entity, arm);
                }
            });
        }
    }
}
