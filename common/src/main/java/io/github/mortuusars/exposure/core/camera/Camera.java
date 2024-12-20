package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Camera {
    protected final PhotographerEntity photographer;
    protected final CameraID cameraID;

    public Camera(PhotographerEntity photographer, CameraID cameraID) {
        this.photographer = photographer;
        this.cameraID = cameraID;
    }

    public PhotographerEntity getPhotographer() {
        return photographer;
    }

    public CameraID getCameraID() {
        return cameraID;
    }

    public abstract ItemStack getItemStack();

    public boolean isEmpty() {
        ItemStack stack = getItemStack();
        return stack.isEmpty() || !(stack.getItem() instanceof CameraItem);
    }

    public boolean isActive() {
        return map(CameraItem::isActive, false);
    }

    public boolean idMatches(CameraID id) {
        return cameraID.equals(id);
    }

    public boolean isShutterOpen() {
        return map((item, stack) -> item.getShutter().isOpen(stack), false);
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

    public <T> Optional<T> map(Function<ItemStack, T> map) {
        ItemStack stack = getItemStack();
        if (stack.getItem() instanceof CameraItem) {
            return Optional.ofNullable(map.apply(stack));
        }
        return Optional.empty();
    }

    public <T> T map(Function<ItemStack, T> map, T orElse) {
        ItemStack stack = getItemStack();
        if (stack.getItem() instanceof CameraItem) {
            return map.apply(stack);
        }
        return orElse;
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
