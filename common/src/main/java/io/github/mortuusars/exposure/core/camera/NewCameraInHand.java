package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.client.SetActiveInHandCameraS2CP;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class NewCameraInHand extends NewCamera {
    protected final InteractionHand hand;

    public NewCameraInHand(LivingEntity owner, InteractionHand hand) {
        super(owner.getItemInHand(hand), owner);
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public IPacket createClientboundPacket() {
        return new SetActiveInHandCameraS2CP(getOwner().getUUID(), hand);
    }
}
