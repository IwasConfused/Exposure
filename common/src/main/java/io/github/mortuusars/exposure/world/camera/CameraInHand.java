package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.client.ActiveCameraInHandSetS2CP;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CameraInHand extends Camera {
    protected final InteractionHand hand;

    public CameraInHand(CameraHolder holder, CameraId cameraId, InteractionHand hand) {
        super(holder, cameraId);
        this.hand = hand;
        if (!(holder instanceof LivingEntity)) {
            throw new IllegalStateException("Only LivingEntity can hold camera in hand."
                    + EntityType.getKey(holder.asEntity().getType()) + " does snot have hands.");
        }
    }

    @Override
    public ItemStack getItemStack() {
        return ((LivingEntity) getHolder()).getItemInHand(getHand());
    }

    @Override
    public Packet createSyncPacket() {
        return new ActiveCameraInHandSetS2CP(getHolder().asEntity().getId(), getId(), getHand());
    }

    public InteractionHand getHand() {
        return hand;
    }

    // --

    public static @Nullable CameraInHand find(CameraHolder holder) {
        if (holder.asEntity() instanceof LivingEntity entity) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack itemInHand = entity.getItemInHand(hand);
                if (itemInHand.getItem() instanceof CameraItem cameraItem) {
                    return new CameraInHand(holder, cameraItem.getOrCreateID(itemInHand), hand);
                }
            }
        }
        return null;
    }
}