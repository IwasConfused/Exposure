package io.github.mortuusars.exposure.neoforge.enumextension;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.ModelPoses;
import net.minecraft.client.model.HumanoidModel;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public class CameraDisassembledArmPose {
    private static final IArmPoseTransformer ARM_POSE_TRANSFORMER = ModelPoses::applyCameraDisassembledPose;

    public static final EnumProxy<HumanoidModel.ArmPose> ENUM_PARAMS =
            new EnumProxy<>(HumanoidModel.ArmPose.class, false, ARM_POSE_TRANSFORMER);

    public static HumanoidModel.ArmPose value() {
        return HumanoidModel.ArmPose.valueOf(Exposure.ID + "_CAMERA_DISASSEMBLED");
    }
}
