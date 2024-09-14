package io.github.mortuusars.exposure.client.animation;

import io.github.mortuusars.exposure.PlatformHelper;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class ModelPoses {
    public static void applyCameraPose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        if (PlatformHelper.isModLoaded("realcamera")) {
            return;
        }

        model.head.xRot += 0.4f; // If we turn head down completely - arms will be too low.
        if (arm == HumanoidArm.RIGHT) {
            AnimationUtils.animateCrossbowHold(model.rightArm, model.leftArm, model.head, true);
        } else if (arm == HumanoidArm.LEFT) {
            AnimationUtils.animateCrossbowHold(model.rightArm, model.leftArm, model.head, false);
        }
        model.head.xRot += 0.3f;
    }

    public static void applyCameraSelfiePose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm, boolean undoArmBobbing) {
        ModelPart cameraArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        // Arm follows camera:
        cameraArm.xRot = (model.head.xRot + Math.abs(model.head.xRot * 0.13f)) + (-(float) Math.PI / 2F);
        cameraArm.yRot = model.head.yRot + (arm == HumanoidArm.RIGHT ? -0.25f : 0.25f);
        if (model.head.xRot <= 0) {
            cameraArm.zRot = -(model.head.xRot * 0.15f);
        } else {
            cameraArm.zRot = -(model.head.xRot * 0.22f);
        }

        if (undoArmBobbing) {
            AnimationUtils.bobModelPart(cameraArm, entity.tickCount, arm == HumanoidArm.LEFT ? 1.0F : -1.0F);
        }
    }
}
