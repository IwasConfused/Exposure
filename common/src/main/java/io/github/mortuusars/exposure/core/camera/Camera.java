package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.item.OldCameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class Camera<T extends OldCameraItem> extends ItemAndStack<T> {
    public Camera(ItemStack stack) {
        super(stack);
    }

    public Optional<CameraInHand<?>> inHand() {
        return this instanceof CameraInHand<T> cameraInHand ? Optional.of(cameraInHand) : Optional.empty();
    }

    public boolean isActive() {
        return getItem().isActive(getItemStack());
    }

    public void activate(Entity entity) {
        getItem().activate(entity, getItemStack());
    }

    public void deactivate(Entity entity) {
        getItem().deactivate(entity, getItemStack());
    }
}
