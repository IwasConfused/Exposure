package io.github.mortuusars.exposure.sound.instance;

import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.core.NewCamera;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.component.camera.ShutterState;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ShutterTimerTickingSoundInstance extends EntityBoundSoundInstance {
    protected final CameraAccessor cameraAccessor;
    protected final Entity entity;
    protected final float fullVolume;
    protected final int durationTicks;
    protected final long endsAtTick;

    public ShutterTimerTickingSoundInstance(CameraAccessor cameraAccessor, Entity sourceEntity, SoundEvent soundEvent,
                                            SoundSource soundSource, float volume, float pitch, int durationTicks, long seed) {
        super(soundEvent, soundSource, volume, pitch, sourceEntity, seed);
        this.cameraAccessor = cameraAccessor;
        this.entity = sourceEntity;
        this.fullVolume = volume;
        this.durationTicks = durationTicks;
        this.endsAtTick = sourceEntity.level().getGameTime() + durationTicks;

        this.looping = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (endsAtTick - entity.level().getGameTime() < 0) {
            stop();
            return;
        }

        Optional<NewCamera> cameraOpt = cameraAccessor.getCamera(entity);
        if (cameraOpt.isEmpty()) {
            if (!(entity instanceof Player player)) {
                stop();
                return;
            }

            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof CameraItem cameraItem) {
                    ShutterState hotbarShutterState = cameraItem.getShutterState(stack);
                    if (hotbarShutterState.isOpen() && hotbarShutterState.shutterSpeed().shouldCauseTickingSound()) {
                        volume = fullVolume * 0.35f;
                        return;
                    }
                }
            }

            // Not stopping to resume sound if camera becomes available again.
            volume = fullVolume * 0.01f;
        }
        else {
            volume = fullVolume;
            NewCamera camera = cameraOpt.get();
            ShutterState shutterState = camera.getItem().getShutterState(camera.getItemStack());
            if (!shutterState.isOpen() || !shutterState.shutterSpeed().shouldCauseTickingSound()) {
                stop();
            }
        }
    }
}
