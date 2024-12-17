package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class Camera {
    private final LivingEntity owner;

    public Camera(LivingEntity owner) {
        this.owner = owner;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public abstract ItemStack getItemStack();

    public boolean isEmpty() {
        ItemStack stack = getItemStack();
        return stack.isEmpty() || !(stack.getItem() instanceof CameraItem);
    }

    public boolean isActive() {
        return map(CameraItem::isActive, false);
    }

    // --

    public Camera ifPresent(BiConsumer<CameraItem, ItemStack> ifPresent) {
        ItemStack stack = getItemStack();
        if (stack.getItem() instanceof CameraItem item) {
            ifPresent.accept(item, stack);
        }
        return this;
    }

    public Camera ifPresent(BiConsumer<CameraItem, ItemStack> ifPresent, Runnable orElse) {
        ItemStack stack = getItemStack();
        if (stack.getItem() instanceof CameraItem item) {
            ifPresent.accept(item, stack);
        } else {
            orElse.run();
        }
        return this;
    }

    public <T> Optional<T> map(BiFunction<CameraItem, ItemStack, T> map) {
        ItemStack stack = getItemStack();
        if (stack.getItem() instanceof CameraItem item) {
            return Optional.ofNullable(map.apply(item, stack));
        }
        return Optional.empty();
    }

    public <T> T map(BiFunction<CameraItem, ItemStack, T> map, T orElse) {
        ItemStack stack = getItemStack();
        if (stack.getItem() instanceof CameraItem item) {
            return map.apply(item, stack);
        }
        return orElse;
    }
}
