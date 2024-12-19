package io.github.mortuusars.exposure.sound.instance;

import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.item.CameraItem;
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

        if (endsAtTick - photographer.asEntity().level().getGameTime() < 0) {
            stop();
            return;
        }

        Camera activeCamera = photographer.activeExposureCamera();
        if (activeCamera != null && activeCamera.idMatches(cameraID)) {
            volume = fullVolume;
            return;
        }

        if (photographer.asEntity() instanceof Player player && isCameraOnHotbar(player)) {
            volume = fullVolume * 0.35f;
        } else {
            // Not stopping to resume sound if camera becomes available again.
            volume = fullVolume * 0.01f;
        }
    }

    protected boolean isCameraOnHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof CameraItem cameraItem && cameraItem.getOrCreateID(stack).equals(cameraID)) {
                return true;
            }
        }
        return false;
    }
}
