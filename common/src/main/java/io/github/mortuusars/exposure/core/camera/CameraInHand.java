package io.github.mortuusars.exposure.core.camera;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CameraInHand extends Camera {
    protected final InteractionHand hand;

    public CameraInHand(LivingEntity owner, InteractionHand hand) {
        super(owner);
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public ItemStack getItemStack() {
        return getOwner().getItemInHand(getHand());
    }
}
