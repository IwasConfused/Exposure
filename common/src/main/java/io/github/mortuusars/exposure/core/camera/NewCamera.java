package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.world.item.ItemStack;

public class NewCamera<T extends CameraItem> extends ItemAndStack<T> {
    public NewCamera(ItemStack stack) {
        super(stack);
    }

    public boolean isActive() {
        return getItem().isActive(getItemStack());
    }
}
