package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import io.github.mortuusars.exposure.world.item.CameraItem;
import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CameraInHand extends Camera {
    protected final InteractionHand hand;

    public CameraInHand(PhotographerEntity photographer, CameraID cameraID, InteractionHand hand) {
        super(photographer, cameraID);
        this.hand = hand;
        if (!(photographer.asEntity() instanceof LivingEntity)) {
            throw new IllegalStateException("Only LivingEntity photographer can hold camera in hand."
                    + EntityType.getKey(photographer.asEntity().getType()) + " does snot have hands.");
        }
    }

    public static CameraInHand find(PhotographerEntity photographer) {
        if (photographer.asEntity() instanceof LivingEntity livingEntity) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack itemInHand = livingEntity.getItemInHand(hand);
                if (itemInHand.getItem() instanceof CameraItem cameraItem) {
                    return new CameraInHand(photographer, cameraItem.getOrCreateID(itemInHand), hand);
                }
            }
        }

        return new CameraInHand.Empty(photographer);
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public ItemStack getItemStack() {
        return ((LivingEntity) getPhotographer().asEntity()).getItemInHand(getHand());
    }

    public static class Empty extends CameraInHand {
        public Empty(PhotographerEntity photographer) {
            super(photographer, new CameraID(Util.NIL_UUID), InteractionHand.MAIN_HAND);
        }

        @Override
        public ItemStack getItemStack() {
            return ItemStack.EMPTY;
        }
    }
}
