package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.animation.ModelPoses;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> {
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightArm;

    // Allows reducing/removing arm bobbing. Doing the bobbing in reverse does not work correctly - arms shiver for some reason.
    @Unique
    private float exposure$LeftArmBobbingMultiplier = 1F;
    @Unique
    private float exposure$RightArmBobbingMultiplier = 1F;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V", ordinal = 0))
    void onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof CameraOperator operator)) {
            exposure$LeftArmBobbingMultiplier = 1F;
            exposure$RightArmBobbingMultiplier = 1F;
            return;
        }

        operator.getActiveExposureCameraOptional().ifPresentOrElse(
                camera -> {
                    HumanoidArm arm = camera instanceof CameraInHand cameraInHand && cameraInHand.getHand() == InteractionHand.OFF_HAND
                            ? Minecraft.getInstance().options.mainHand().get().getOpposite()
                            : Minecraft.getInstance().options.mainHand().get();

                    camera.ifPresent((item, stack) -> {
                        if (item.isInSelfieMode(stack)) {
                            ModelPoses.applyCameraSelfiePose((HumanoidModel<?>) (Object) this, entity, arm, false);

                            if (arm == HumanoidArm.LEFT) {
                                exposure$LeftArmBobbingMultiplier = 0F;
                                exposure$RightArmBobbingMultiplier = 1F;
                            } else {
                                exposure$LeftArmBobbingMultiplier = 1F;
                                exposure$RightArmBobbingMultiplier = 0F;
                            }
                        } else {
                            ModelPoses.applyCameraPose(((HumanoidModel<?>) (Object) this), entity, arm);

                            if (arm == HumanoidArm.LEFT) {
                                exposure$LeftArmBobbingMultiplier = 0.1F;
                                exposure$RightArmBobbingMultiplier = 0.5F;
                            } else {
                                exposure$LeftArmBobbingMultiplier = 0.5F;
                                exposure$RightArmBobbingMultiplier = 0.1F;
                            }
                        }
                    });
                },
                () -> {
                    CameraInHand camera = CameraInHand.find(entity);
                    camera.ifPresent((item, stack) -> {
                        if (item.isDisassembled(stack)) {
                            HumanoidArm arm = camera.getHand() == InteractionHand.OFF_HAND
                                    ? Minecraft.getInstance().options.mainHand().get().getOpposite()
                                    : Minecraft.getInstance().options.mainHand().get();
                            ModelPoses.applyCameraDisassembledPose((HumanoidModel<?>) (Object) this, entity, arm);
                        }
                    });
                    exposure$LeftArmBobbingMultiplier = 1F;
                    exposure$RightArmBobbingMultiplier = 1F;
                }
        );
    }

    @Redirect(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V"))
    private void removeBobbing(ModelPart modelPart, float ageInTicks, float multiplier) {
        if (modelPart == leftArm) {
            multiplier = exposure$LeftArmBobbingMultiplier;
        }
        if (modelPart == rightArm) {
            multiplier = exposure$RightArmBobbingMultiplier * -1;
        }
        AnimationUtils.bobModelPart(modelPart, ageInTicks, multiplier);
    }
}
