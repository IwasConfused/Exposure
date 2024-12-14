package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;

public class CameraInHand<T extends CameraItem> extends Camera<T> {
    private final InteractionHand hand;

    public CameraInHand(ItemStack stack, InteractionHand hand) {
        super(stack);
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public <R> R map(TriFunction<T, ItemStack, InteractionHand, R> func) {
        return func.apply(getItem(), getItemStack(), getHand());
    }
}
