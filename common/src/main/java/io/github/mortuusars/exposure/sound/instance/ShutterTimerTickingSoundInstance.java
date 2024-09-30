package io.github.mortuusars.exposure.sound.instance;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShutterTimerTickingSoundInstance extends EntityBoundSoundInstance {
    protected final Entity entity;
//    protected final Entity entity;
//    protected int delay = 2;
//    protected final float originalVolume;

    protected long shouldEndAt = -1;

    public ShutterTimerTickingSoundInstance(Entity entity, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, long seed) {
        super(soundEvent, soundSource, volume, pitch, entity, seed);
        this.entity = entity;
        this.looping = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (shouldEndAt < 0) {
            shouldEndAt = UnixTimestamp.Seconds.fromNow(30); // To not play indefinitely if not cancelled.
        }

        if (UnixTimestamp.Seconds.now() - shouldEndAt > 0) {
            stop();
        }


        //TODO: stopping
//        if (hasShutterOpen(player.getMainHandItem()) || hasShutterOpen(player.getOffhandItem())) {
//            volume = Mth.lerp(0.3f, volume, originalVolume);
//            return;
//        }
//        else
//            volume = Mth.lerp(0.2f, volume, originalVolume * 0.3f);
//
//        if (!hasCameraWithOpenShutterInInventory(player)) {
//            // In multiplayer other players camera photo is not updated in time (sometimes)
//            // This causes the sound to stop instantly
//            if (!player.equals(Minecraft.getInstance().player) && delay > 0) {
//                delay--;
//                return;
//            }
//
//            this.stop();
//        }
    }

//    private boolean hasCameraWithOpenShutterInInventory(Player player) {
//        for (ItemStack stack : player.getInventory().items) {
//            if (hasShutterOpen(stack))
//                return true;
//        }
//
//        return false;
//    }

//    private boolean hasShutterOpen(ItemStack stack) {
//        return stack.getItem() instanceof CameraItem cameraItem && cameraItem.getShutterState(stack).isOpen();
//    }
}
