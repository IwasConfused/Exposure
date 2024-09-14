package io.github.mortuusars.exposure.neoforge.item;

import io.github.mortuusars.exposure.item.CameraItem;
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
    public HumanoidModel.@Nullable ArmPose getArmPose(@NotNull LivingEntity entityLiving,
                                                      @NotNull InteractionHand hand, @NotNull ItemStack itemStack) {
        if (entityLiving instanceof Player
                && itemStack.getItem() instanceof CameraItem cameraItem
                && cameraItem.isActive(itemStack)) {
            if (cameraItem.isInSelfieMode(itemStack))
                return CameraSelfieArmPose.value();
            else
                return CameraArmPose.value();
        }

        return HumanoidModel.ArmPose.ITEM;
    }
}
