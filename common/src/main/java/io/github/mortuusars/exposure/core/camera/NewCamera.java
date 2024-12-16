package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public abstract class NewCamera extends ItemAndStack<CameraItem> {
    private final LivingEntity owner;

    public NewCamera(ItemStack stack, LivingEntity owner) {
        super(stack);
        this.owner = owner;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public Vec3 getPosition() {
        return owner.position();
    }

    public boolean isActive() {
        return getItem().isActive(getItemStack());
    }

//    public abstract void activate(LivingEntity entity) {
//        getItem().activate(entity, getItemStack());
//    }
//
//    public void deactivate(LivingEntity entity) {
//        getItem().deactivate(entity, getItemStack());
//    }

    public abstract IPacket createClientboundPacket();

//    public abstract boolean isInHand();
//    public abstract InteractionHand getHand();
}
