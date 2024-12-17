package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.item.OldCameraItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;

public class OtherCameraInHand<T extends OldCameraItem> extends OtherCamera<T> {
    private final InteractionHand hand;

    public OtherCameraInHand(ItemStack stack, InteractionHand hand) {
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
