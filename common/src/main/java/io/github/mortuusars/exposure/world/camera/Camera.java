package io.github.mortuusars.exposure.world.camera;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.CameraItem;
import io.github.mortuusars.exposure.world.item.part.Attachment;
import io.github.mortuusars.exposure.world.item.part.CameraSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Camera {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Entity owner;
    protected final CameraId id;

    public Camera(Entity owner, CameraId id) {
        this.owner = owner;
        this.id = id;
    }

    public abstract ItemStack getItemStack();
    public abstract Packet createSyncPacket();

    // --

    public Entity getOwner() {
        return owner;
    }

    public CameraId getId() {
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

    public boolean idMatches(CameraId id) {
        return this.id.equals(id);
    }

    public boolean isShutterOpen() {
        return map((item, stack) -> item.getShutter().isOpen(stack), false);
    }

    public void release() {
        if (!(getOwner() instanceof CameraHolder holder)) {
            LOGGER.error("Cannot release: owner is not a Camera Holder: {}", owner);
            return;
        }

        ifPresent((item, stack) -> item.release(holder, getItemStack()),
                () -> LOGGER.error("Cannot take a shot: camera is not active. Camera Holder: {}", owner));
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
}
