package io.github.mortuusars.exposure.item.part;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.item.component.camera.ShutterState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class Shutter {
    protected BiConsumer<Entity, ItemStack> onClosed = (entity, stack) -> { };

    /**
     * Will not be executed when closing time should've been "long" ago.
     * When camera wasn't in inventory at the time of closing, for example.
     */
    public void onClosed(BiConsumer<Entity, ItemStack> onClosed) {
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

    public void tick(Entity entity, ItemStack stack) {
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

    public void open(Entity entity, ItemStack stack, ShutterSpeed shutterSpeed) {
        setState(stack, ShutterState.open(entity.level().getGameTime(), shutterSpeed));
        entity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playOpenSound(entity);
    }

    public void close(Entity entity, ItemStack stack) {
        setState(stack, ShutterState.closed());
        entity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playCloseSound(entity);
        onClosed.accept(entity, stack);

//        StoredItemStack filmStack = getAttachment(stack, AttachmentType.FILM);
//        if (filmStack.getItem() instanceof FilmRollItem filmRollItem) {
//            float fullness = filmRollItem.getFullness(filmStack.getForReading());
//            boolean isFull = fullness == 1f;
//
//            if (isFull)
//                OnePerEntitySounds.play(null, player, Exposure.SoundEvents.FILM_ADVANCE_LAST.get(), SoundSource.PLAYERS, 1f, 1f);
//            else {
//                OnePerEntitySounds.play(null, player, Exposure.SoundEvents.FILM_ADVANCING.get(), SoundSource.PLAYERS,
//                        1f, 0.9f + 0.1f * fullness);
//            }
//        }
    }

    public void playOpenSound(Entity entity) {
        playSound(entity, Exposure.SoundEvents.SHUTTER_OPEN.get(), 0.7f, 1.1f, 0.2f);
    }

    public void playCloseSound(Entity entity) {
        playSound(entity, Exposure.SoundEvents.SHUTTER_CLOSE.get(), 0.7f, 1.1f, 0.2f);
    }

    private void playSound(@NotNull Entity sourceEntity, SoundEvent sound, float volume, float pitch, float pitchVariety) {
        if (pitchVariety > 0f)
            pitch = pitch - (pitchVariety / 2f) + (sourceEntity.getRandom().nextFloat() * pitchVariety);
        sourceEntity.level().playSound(null, sourceEntity, sound, SoundSource.PLAYERS, volume, pitch);
    }
}
