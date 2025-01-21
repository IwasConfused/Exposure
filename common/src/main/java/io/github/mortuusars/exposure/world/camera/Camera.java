package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.CameraItem;
import io.github.mortuusars.exposure.world.item.part.Attachment;
import io.github.mortuusars.exposure.world.item.part.CameraSetting;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Camera {
    protected final CameraHolder holder;
    protected final CameraID id;

    public Camera(CameraHolder holder, CameraID id) {
        this.holder = holder;
        this.id = id;
    }

    public abstract ItemStack getItemStack();

    // --

    public CameraHolder getHolder() {
        return holder;
    }

    public CameraID getId() {
        return id;
    }

    // --

    public void update() {

    }

    public boolean isEmpty() {
        return !(getItemStack().getItem() instanceof CameraItem);
    }

    public boolean isActive() {
        return map(CameraItem::isActive, false);
    }

    public boolean idMatches(CameraID id) {
        return this.id.equals(id);
    }

    public boolean isShutterOpen() {
        return map((item, stack) -> item.getShutter().isOpen(stack), false);
    }

    public void release() {
        ifPresent((item, stack) -> item.release(getHolder(), getItemStack()),
                () -> Exposure.LOGGER.error("Cannot take a shot: camera is not active. Camera Holder: {}", holder.asEntity()));
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

    public <T extends Item, R> Optional<R> mapAttachment(Attachment<T> attachment, BiFunction<T, ItemStack, R> func) {
        if (getItemStack().getItem() instanceof CameraItem) {
            return attachment.map(getItemStack(), func);
        }
        return Optional.empty();
    }

    public <T> Optional<T> getSetting(CameraSetting<T> setting) {
        if (getItemStack().getItem() instanceof CameraItem) {
            return setting.getOptional(getItemStack());
        }
        return Optional.empty();
    }

    // --

    public static class Empty extends Camera {
        public Empty(CameraHolder holder) {
            super(holder, new CameraID(Util.NIL_UUID));
        }

        @Override
        public ItemStack getItemStack() {
            return ItemStack.EMPTY;
        }
    }
}
