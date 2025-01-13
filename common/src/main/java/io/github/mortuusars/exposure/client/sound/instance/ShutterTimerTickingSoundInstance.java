package io.github.mortuusars.exposure.client.sound.instance;

import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import io.github.mortuusars.exposure.world.item.CameraItem;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShutterTimerTickingSoundInstance extends EntityBoundSoundInstance {
    protected final PhotographerEntity photographer;
    protected final CameraID cameraID;
    protected final float fullVolume;
    protected final int durationTicks;
    protected final long endsAtTick;

    public ShutterTimerTickingSoundInstance(PhotographerEntity photographer, CameraID cameraID, SoundEvent soundEvent,
                                            SoundSource soundSource, float volume, float pitch, int durationTicks) {
        super(soundEvent, soundSource, volume, pitch, photographer.asEntity(), photographer.asEntity().getRandom().nextLong());
        this.photographer = photographer;
        this.cameraID = cameraID;
        this.fullVolume = volume;
        this.durationTicks = durationTicks;
        this.endsAtTick = photographer.asEntity().level().getGameTime() + durationTicks;

        this.looping = true;
    }

    @Override
    public void tick() {
        super.tick();

        //TODO: refactor

        if (endsAtTick - photographer.asEntity().level().getGameTime() < 0) {
            stop();
            return;
        }

        if (photographer.getActiveExposureCamera().map(camera ->
                camera.getCameraID().equals(cameraID) && !camera.isShutterOpen()).orElse(false)) {
            stop();
            return;
        }

        Camera activeCamera = photographer.activeExposureCamera();
        if (activeCamera != null && activeCamera.idMatches(cameraID)) {
            volume = fullVolume;
            return;
        }

        if (photographer.asEntity() instanceof Player player) {
            ItemStack cameraOnHotbar = getCameraOnHotbar(player);
            if (cameraOnHotbar.isEmpty()) {
                // Not stopping to resume sound if camera becomes available again.
                volume = fullVolume * 0.01f;
            } else if (cameraOnHotbar.getItem() instanceof CameraItem cameraItem && cameraItem.getShutter().isOpen(cameraOnHotbar)) {
                volume = fullVolume * 0.35f;
            } else {
                stop();
            }
        } else {
            // Not stopping to resume sound if camera becomes available again.
            volume = fullVolume * 0.01f;
        }
    }

    protected boolean isCameraOnHotbar(Player player) {
        return !getCameraOnHotbar(player).isEmpty();
    }

    protected ItemStack getCameraOnHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (CameraID.ofStack(stack).equals(cameraID)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
