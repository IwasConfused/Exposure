package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.client.ActiveCameraInHandSetS2CP;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.CameraItem;
import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CameraInHand extends Camera {
    protected final InteractionHand hand;

    public CameraInHand(Entity owner, CameraId cameraId, InteractionHand hand) {
        super(owner, cameraId);
        this.hand = hand;
        if (!(owner instanceof LivingEntity)) {
            throw new IllegalStateException("Only LivingEntity can hold camera in hand."
                    + EntityType.getKey(owner.getType()) + " does snot have hands.");
        }
    }

    @Override
    public ItemStack getItemStack() {
        return ((LivingEntity) getOwner()).getItemInHand(getHand());
    }

    @Override
    public Packet createSyncPacket() {
        return new ActiveCameraInHandSetS2CP(getOwner().getId(), getId(), getHand());
    }

    public InteractionHand getHand() {
        return hand;
    }

    // --

    public static CameraInHand find(LivingEntity entity) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = entity.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem cameraItem) {
                return new CameraInHand(entity, cameraItem.getOrCreateID(itemInHand), hand);
            }
        }

        return new CameraInHand.Empty(entity);
    }

    public static class Empty extends CameraInHand {
        public Empty(LivingEntity entity) {
            super(entity, new CameraId(Util.NIL_UUID), InteractionHand.MAIN_HAND);
        }

        @Override
        public ItemStack getItemStack() {
            return ItemStack.EMPTY;
        }
    }
}