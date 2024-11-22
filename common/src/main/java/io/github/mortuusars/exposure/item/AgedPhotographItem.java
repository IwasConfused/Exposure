package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.core.PhotographType;
import net.minecraft.world.item.ItemStack;

public class AgedPhotographItem extends PhotographItem {
    public AgedPhotographItem(Properties properties) {
        super(properties);
    }

    @Override
    public PhotographType getType(ItemStack stack) {
        return PhotographType.AGED;
    }
}
