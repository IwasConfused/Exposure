package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.CameraItem;
import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CameraInHand extends Camera {
    protected final InteractionHand hand;

    public CameraInHand(CameraHolder holder, CameraID cameraID, InteractionHand hand) {
        super(holder, cameraID);
        this.hand = hand;
        if (!(holder instanceof LivingEntity)) {
            throw new IllegalStateException("Only LivingEntity can hold camera in hand."
                    + EntityType.getKey(holder.asEntity().getType()) + " does snot have hands.");
        }
    }

    @Override
    public ItemStack getItemStack() {
        return ((LivingEntity) getHolder().asEntity()).getItemInHand(getHand());
    }

    public InteractionHand getHand() {
        return hand;
    }

    // --

    public static CameraInHand find(CameraHolder holder) {
        if (holder instanceof LivingEntity entity) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack itemInHand = entity.getItemInHand(hand);
                if (itemInHand.getItem() instanceof CameraItem cameraItem) {
                    return new CameraInHand(holder, cameraItem.getOrCreateID(itemInHand), hand);
                }
            }
        }

        return new CameraInHand.Empty(holder);
    }

    public static class Empty extends CameraInHand {
        public Empty(CameraHolder holder) {
            super(holder, new CameraID(Util.NIL_UUID), InteractionHand.MAIN_HAND);
        }

        @Override
        public ItemStack getItemStack() {
            return ItemStack.EMPTY;
        }
    }
}