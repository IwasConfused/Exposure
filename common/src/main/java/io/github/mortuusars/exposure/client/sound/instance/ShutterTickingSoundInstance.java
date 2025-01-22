package io.github.mortuusars.exposure.client.sound.instance;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.CameraItem;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShutterTickingSoundInstance extends EntityBoundSoundInstance {
    protected final CameraId cameraId;
    protected final float fullVolume;
    protected final int durationTicks;
    protected final long endsAtTick;

    protected Entity entity;

    public ShutterTickingSoundInstance(Entity entity, CameraId cameraId, SoundEvent soundEvent,
                                       SoundSource soundSource, float volume, float pitch, int durationTicks) {
        super(soundEvent, soundSource, volume, pitch, entity, entity.getRandom().nextLong());
        this.entity = entity;
        this.cameraId = cameraId;
        this.fullVolume = volume;
        this.durationTicks = durationTicks;
        this.endsAtTick = entity.level().getGameTime() + durationTicks;
        this.looping = true;
        this.volume = 0;
    }

    public void updateEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canPlaySound() {
        return !this.entity.isSilent();
    }

    @Override
    public void tick() {
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();

        if (endsAtTick - entity.level().getGameTime() < 0) {
            stop();
            Exposure.LOGGER.info("Stopped ticking sound.");
            return;
        }

        ItemStack stack = ItemStack.EMPTY;
        boolean isOnHotbar = false;

        if (entity instanceof Player player) {
            CameraInHand cameraInHand = CameraInHand.find(player);
            if (!cameraInHand.isEmpty() && cameraInHand.idMatches(cameraId)) {
                stack = cameraInHand.getItemStack();
            } else {
                ItemStack hotbarStack = getCameraOnHotbar(player);
                if (!hotbarStack.isEmpty()) {
                    stack = hotbarStack;
                    isOnHotbar = true;
                }
            }
        } else if (entity instanceof LivingEntity livingEntity) {
            CameraInHand cameraInHand = CameraInHand.find(livingEntity);
            if (!cameraInHand.isEmpty() && cameraInHand.idMatches(cameraId)) {
                stack = cameraInHand.getItemStack();
            }
        } else if (entity instanceof ItemEntity itemEntity) {
            if (itemEntity.getItem().getItem() instanceof CameraItem)
                stack = itemEntity.getItem();
        }

        if (stack.getItem() instanceof CameraItem item && item.getShutter().isOpen(stack)) {
            volume = isOnHotbar ? fullVolume * 0.35f : fullVolume;
        } else {
            volume = 0;
        }
    }

    protected ItemStack getCameraOnHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (cameraId.matches(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
