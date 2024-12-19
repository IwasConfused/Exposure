package io.github.mortuusars.exposure.core.camera;

import com.google.common.base.Preconditions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CameraInHand extends Camera {
    protected final InteractionHand hand;

    public CameraInHand(PhotographerEntity photographer, CameraID cameraID, InteractionHand hand) {
        super(photographer, cameraID);
        this.hand = hand;
        Preconditions.checkState(photographer.asEntity() instanceof LivingEntity,
                "Only LivingEntity photographer can hold camera in hand. %s does snot have hands.",
                EntityType.getKey(photographer.asEntity().getType()));
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public ItemStack getItemStack() {
        return ((LivingEntity) getPhotographer().asEntity()).getItemInHand(getHand());
    }
}
