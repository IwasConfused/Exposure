package io.github.mortuusars.exposure.item.part;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.item.component.camera.ShutterState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class Shutter {
    protected BiConsumer<LivingEntity, ItemStack> onOpen = (entity, stack) -> { };
    protected BiConsumer<LivingEntity, ItemStack> onClosed = (entity, stack) -> { };

    public void onOpen(BiConsumer<LivingEntity, ItemStack> onOpen) {
        this.onOpen = onOpen;
    }

    /**
     * Will not be executed when closing time should've been "long" ago.
     * When camera wasn't in inventory at the time of closing, for example.
     */
    public void onClosed(BiConsumer<LivingEntity, ItemStack> onClosed) {
        this.onClosed = onClosed;
    }

    public ShutterState getState(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.SHUTTER_STATE, ShutterState.CLOSED);
    }

    public void setState(ItemStack stack, ShutterState shutterState) {
        stack.set(Exposure.DataComponents.SHUTTER_STATE, shutterState);
    }

    public boolean isOpen(ItemStack stack) {
        return getState(stack).isOpen();
    }

    public boolean shouldClose(ItemStack stack, long gameTime) {
        ShutterState state = getState(stack);
        return state.isOpen() && gameTime >= state.getCloseTick();
    }

    public void tick(LivingEntity entity, ItemStack stack) {
        long gameTime = entity.level().getGameTime();
        if (shouldClose(stack, gameTime)) {
            ShutterState state = getState(stack);
            if (gameTime - state.getCloseTick() > 30) {
                setState(stack, ShutterState.CLOSED);
            } else {
                close(entity, stack);
            }
        }
    }

    public void open(LivingEntity entity, ItemStack stack, ShutterSpeed shutterSpeed) {
        setState(stack, ShutterState.open(entity.level().getGameTime(), shutterSpeed));
        entity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playOpenSound(entity);
    }

    public void close(LivingEntity entity, ItemStack stack) {
        setState(stack, ShutterState.closed());
        entity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playCloseSound(entity);
        onClosed.accept(entity, stack);
    }

    public void playOpenSound(LivingEntity entity) {
        playSound(entity, Exposure.SoundEvents.SHUTTER_OPEN.get(), 0.7f, 1.1f, 0.2f);
    }

    public void playCloseSound(LivingEntity entity) {
        playSound(entity, Exposure.SoundEvents.SHUTTER_CLOSE.get(), 0.7f, 1.1f, 0.2f);
    }

    private void playSound(@NotNull LivingEntity sourceEntity, SoundEvent sound, float volume, float pitch, float pitchVariety) {
        if (pitchVariety > 0f)
            pitch = pitch - (pitchVariety / 2f) + (sourceEntity.getRandom().nextFloat() * pitchVariety);
        sourceEntity.level().playSound(null, sourceEntity, sound, SoundSource.PLAYERS, volume, pitch);
    }
}
