package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
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

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public ItemStack getItemStack() {
        return ((LivingEntity) getPhotographer().asEntity()).getItemInHand(getHand());
    }
}
