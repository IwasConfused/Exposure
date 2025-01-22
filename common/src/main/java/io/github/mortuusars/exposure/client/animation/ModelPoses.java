package io.github.mortuusars.exposure.client.animation;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.client.CameraType;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class ModelPoses {
    public static void applyCameraPose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        if (PlatformHelper.isModLoaded("realcamera")) {
            return;
        }

        float actionProgress = getCameraActionAnim(entity);
        actionProgress = (float)EasingFunction.EASE_OUT_CUBIC.ease(actionProgress);
        float actionAnim =  actionProgress > 0.2F ? (1F - actionProgress) : actionProgress;

        boolean rightHanded = arm == HumanoidArm.RIGHT;

        ModelPart mainHand = rightHanded ? model.rightArm : model.leftArm;
        ModelPart offHand = rightHanded ? model.leftArm : model.rightArm;

        model.head.xRot += 0.4f; // Applying part of head rotation. If we turn head down completely - arms will be too low.
        model.head.xRot = Math.min(1.25f, model.head.xRot); // Look down limit

        mainHand.yRot = (rightHanded ? -0.3F : 0.3F) + model.head.yRot;
        offHand.yRot = (rightHanded ? 0.6F : -0.6F) + model.head.yRot;
        mainHand.xRot = (float) (-Math.PI / 2) + model.head.xRot + 0.1F;
        offHand.xRot = -1.5F + model.head.xRot;
        offHand.xRot += actionAnim * 0.05F;
        offHand.yRot += actionAnim * 0.05F;
        offHand.zRot += actionAnim * 0.05F;
        model.head.xRot += 0.3f; // Applying rest of head rotation after arms

        model.hat.copyFrom(model.head);    }

    public static void applyCameraSelfiePose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm, boolean undoArmBobbing) {
        ModelPart cameraArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        // Arm follows camera:
        cameraArm.xRot = (model.head.xRot + Math.abs(model.head.xRot * 0.13f)) + (-(float) Math.PI / 2F);
        cameraArm.yRot = model.head.yRot + (arm == HumanoidArm.RIGHT ? -0.25f : 0.25f);
        if (model.head.xRot <= 0) {
            cameraArm.zRot = (model.head.xRot * 0.15f) * (arm == HumanoidArm.RIGHT ? -1 : 1);
        } else {
            cameraArm.zRot = (model.head.xRot * 0.22f) * (arm == HumanoidArm.RIGHT ? -1 : 1);
        }

//        if (undoArmBobbing) {
//            AnimationUtils.bobModelPart(cameraArm, entity.tickCount, arm == HumanoidArm.LEFT ? 1.0F : -1.0F);
//        }
    }

    public static void applyCameraDisassembledPose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        if (Minecrft.player().equals(entity) && Minecrft.options().getCameraType() == CameraType.FIRST_PERSON) {
            return;
        }

        float actionProgress = getCameraActionAnim(entity);
        actionProgress = (float)EasingFunction.EASE_OUT_CUBIC.ease(actionProgress);
        float actionAnim =  actionProgress > 0.2F ? (1F - actionProgress) : actionProgress;

        model.head.xRot += 0.4f; // Applying part of head rotation. If we turn head down completely - arms will be too low.
        model.head.xRot = Math.min(0.75f, model.head.xRot); // Look down limit

        boolean rightHanded = arm == HumanoidArm.RIGHT;

        ModelPart mainHand = rightHanded ? model.rightArm : model.leftArm;
        ModelPart offHand = rightHanded ? model.leftArm : model.rightArm;
        mainHand.yRot = (rightHanded ? -0.6F : 0.6F) + model.head.yRot;
        offHand.yRot = (rightHanded ? 0.6F : -0.6F) + model.head.yRot;
        mainHand.xRot = (float) (-Math.PI / 2) + model.head.xRot + 0.1F;
        mainHand.zRot -= rightHanded ? -0.5f : 0.5f;
        offHand.xRot = -1.5F + model.head.xRot;
        offHand.xRot += actionAnim * 0.05F;
        offHand.yRot += actionAnim * 0.05F;
        offHand.zRot += actionAnim * 0.05F;
        model.head.xRot += 0.3f; // Applying rest of head rotation after arms

        model.hat.copyFrom(model.head);
    }

    public static float getCameraActionAnim(LivingEntity entity) {
        if (entity instanceof CameraOperator operator) {
            float partialTick = Minecrft.get().getTimer().getGameTimeDeltaPartialTick(true);
            return operator.getExposureCameraActionAnim(partialTick);
        }
        return 0F;
    }
}
