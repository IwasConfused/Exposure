package io.github.mortuusars.exposure.neoforge.item;

import io.github.mortuusars.exposure.neoforge.enumextension.CameraDisassembledArmPose;
import io.github.mortuusars.exposure.world.item.CameraItem;
import io.github.mortuusars.exposure.neoforge.enumextension.CameraArmPose;
import io.github.mortuusars.exposure.neoforge.enumextension.CameraSelfieArmPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CameraItemForgeClientExtensions implements IClientItemExtensions {
    public static final CameraItemForgeClientExtensions INSTANCE = new CameraItemForgeClientExtensions();

    private CameraItemForgeClientExtensions() {
    }

    @Override
    public HumanoidModel.@Nullable ArmPose getArmPose(@NotNull LivingEntity entity,
                                                      @NotNull InteractionHand hand, @NotNull ItemStack stack) {
        if (stack.getItem() instanceof CameraItem cameraItem) {
            if (cameraItem.isActive(stack)) {
                return cameraItem.isInSelfieMode(stack) ? CameraSelfieArmPose.value() : CameraArmPose.value();
            }

            if (cameraItem.isDisassembled(stack)) {
                return CameraDisassembledArmPose.value();
            }

        }

        return HumanoidModel.ArmPose.ITEM;
    }
}
