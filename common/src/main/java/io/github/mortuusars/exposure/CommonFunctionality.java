package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.sound.OnePerEntitySounds;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CommonFunctionality {
    public static void handleItemDrop(@Nullable Player player, @Nullable ItemEntity droppedItemEntity) {
        if (player == null || droppedItemEntity == null)
            return;

        ItemStack stack = droppedItemEntity.getItem();
        if (stack.getItem() instanceof CameraItem cameraItem) {
            if (cameraItem.isActive(stack)) {
                cameraItem.deactivate(player, stack);
            }

            if (cameraItem.getShutterState(stack).isOpen()) {
                OnePerEntitySounds.stopForAllClients(player, Exposure.SoundEvents.SHUTTER_TICKING.get());
            }
        }
    }
}
