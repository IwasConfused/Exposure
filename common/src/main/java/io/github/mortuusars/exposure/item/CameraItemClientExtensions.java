package io.github.mortuusars.exposure.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CameraItemClientExtensions {
    public static float itemPropertyFunction(ItemStack stack, ClientLevel clientLevel, LivingEntity livingEntity, int seed) {
        if (stack.getItem() instanceof OldCameraItem cameraItem && cameraItem.isActive(stack)) {
            if (cameraItem.isInSelfieMode(stack))
                // Longer selfie stick for current player (to not obscure the view) and shorter for everyone else
                return livingEntity == Minecraft.getInstance().player ? 0.2f : 0.3f;

            return 0.1f;
        }

        return 0f;
    }
}
