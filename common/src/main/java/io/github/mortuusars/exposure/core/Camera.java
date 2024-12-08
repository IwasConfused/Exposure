package io.github.mortuusars.exposure.core;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Camera extends ItemAndStack<CameraItem> {
    protected final Optional<InteractionHand> hand;

    public Camera(ItemStack stack, @Nullable InteractionHand hand) {
        super(stack);
        this.hand = Optional.ofNullable(hand);
    }

    public boolean isActive() {
        return getItem().isActive(getItemStack());
    }

    //TODO: move to CameraInHand class
    public Optional<InteractionHand> getHand() {
        return hand;
    }

    public boolean isInHand() {
        return getHand().isPresent();
    }

    //    public void activate(Player player) {
//        getItem().activateViewfinder(player, getItemStack());
//    }
//
//    public void deactivate(Player player) {
//        getItem().deactivateViewfinder(player, getItemStack());
//    }
}
