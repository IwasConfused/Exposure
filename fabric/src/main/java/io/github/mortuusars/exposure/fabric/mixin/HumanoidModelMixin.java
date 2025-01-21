package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.client.animation.ModelPoses;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    void onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof CameraOperator operator) || !(entity instanceof CameraHolder holder))
            return;

        operator.getActiveExposureCamera().filter(Camera::isActive).ifPresentOrElse(
                camera -> {
                    HumanoidArm arm = camera instanceof CameraInHand cameraInHand && cameraInHand.getHand() == InteractionHand.OFF_HAND
                            ? Minecraft.getInstance().options.mainHand().get().getOpposite()
                            : Minecraft.getInstance().options.mainHand().get();

                    camera.ifPresent((item, stack) -> {
                        if (item.isInSelfieMode(stack)) {
                            ModelPoses.applyCameraSelfiePose((HumanoidModel<?>) (Object) this, entity, arm, false);
                        } else {
                            ModelPoses.applyCameraPose(((HumanoidModel<?>) (Object) this), entity, arm);
                        }
                    });
                },
                () -> {
                    CameraInHand camera = CameraInHand.find(((CameraHolder) entity));
                    camera.ifPresent((item, stack) -> {
                        if (item.isDisassembled(stack)) {
                            HumanoidArm arm = camera.getHand() == InteractionHand.OFF_HAND
                                    ? Minecraft.getInstance().options.mainHand().get().getOpposite()
                                    : Minecraft.getInstance().options.mainHand().get();
                            ModelPoses.applyCameraDisassembledPose((HumanoidModel<?>) (Object) this, entity, arm);
                        }
                    });
                }
        );
    }
}
