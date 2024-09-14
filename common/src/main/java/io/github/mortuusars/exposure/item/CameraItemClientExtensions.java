package io.github.mortuusars.exposure.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CameraItemClientExtensions {
    public static float itemPropertyFunction(ItemStack stack, ClientLevel clientLevel, LivingEntity livingEntity, int seed) {
        if (stack.getItem() instanceof CameraItem cameraItem) {
            if (cameraItem.isActive(stack)) {
                if (cameraItem.isInSelfieMode(stack))
                    return livingEntity == Minecraft.getInstance().player ? 0.2f : 0.3f;

                return 0.1f;
            }
        }

        return 0f;
    }

    public static void releaseUseButton() {
        Minecraft.getInstance().options.keyUse.setDown(false);
    }
}
