package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.client.animation.ModelPoses;
import io.github.mortuusars.exposure.core.camera.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> {
    @Shadow
    public abstract ModelPart getHead();

    @Final
    @Shadow
    public ModelPart hat;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    void onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player))
            return;

        CameraClient.getActiveCamera().ifPresent(camera -> {
            if (camera instanceof CameraInHand<?> cameraInHand) {
                HumanoidArm arm = Minecraft.getInstance().options.mainHand().get();
                if (cameraInHand.getHand() == InteractionHand.OFF_HAND)
                    arm = arm.getOpposite();

                if (camera.getItem().isInSelfieMode(camera.getItemStack()))
                    ModelPoses.applyCameraSelfiePose((HumanoidModel<?>) (Object) this, entity, arm, false);
                else
                    ModelPoses.applyCameraPose(((HumanoidModel<?>) (Object) this), entity, arm);

                hat.copyFrom(getHead());
            }
        });
    }
}
