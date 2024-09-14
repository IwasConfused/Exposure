package io.github.mortuusars.exposure.core;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NewCamera extends ItemAndStack<CameraItem> {
    public NewCamera(ItemStack stack) {
        super(stack);
    }

    //    public void activate(Player player) {
//        getItem().activateViewfinder(player, getItemStack());
//    }
//
//    public void deactivate(Player player) {
//        getItem().deactivateViewfinder(player, getItemStack());
//    }
}
