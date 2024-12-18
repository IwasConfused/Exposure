package io.github.mortuusars.exposure.item.part;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.item.component.camera.ShutterState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.BiConsumer;

public class Shutter {
    protected BiConsumer<PhotographerEntity, ItemStack> onOpen = (entity, stack) -> { };
    protected BiConsumer<PhotographerEntity, ItemStack> onClosed = (entity, stack) -> { };

    public void onOpen(BiConsumer<PhotographerEntity, ItemStack> onOpen) {
        this.onOpen = onOpen;
    }

    /**
     * Will not be executed when closing time should've been "long" ago.
     * When camera wasn't in inventory at the time of closing, for example.
     */
    public void onClosed(BiConsumer<PhotographerEntity, ItemStack> onClosed) {
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

    public void tick(PhotographerEntity photographer, ItemStack stack) {
        long gameTime = photographer.asEntity().level().getGameTime();
        if (shouldClose(stack, gameTime)) {
            ShutterState state = getState(stack);
            if (gameTime - state.getCloseTick() > 30) {
                setState(stack, ShutterState.CLOSED);
            } else {
                close(photographer, stack);
            }
        }
    }

    public void open(PhotographerEntity photographer, ItemStack stack, ShutterSpeed shutterSpeed) {
        setState(stack, ShutterState.open(photographer.asEntity().level().getGameTime(), shutterSpeed));
        photographer.asEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playOpenSound(photographer);
    }

    public void close(PhotographerEntity photographer, ItemStack stack) {
        setState(stack, ShutterState.closed());
        photographer.asEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playCloseSound(photographer);
        onClosed.accept(photographer, stack);
    }

    public void playOpenSound(PhotographerEntity photographer) {
        photographer.playCameraSoundNoExclude(Exposure.SoundEvents.SHUTTER_OPEN.get(), 0.7f, 1.1f, 0.2f);
    }

    public void playCloseSound(PhotographerEntity photographer) {
        photographer.playCameraSoundNoExclude(Exposure.SoundEvents.SHUTTER_CLOSE.get(), 0.7f, 1.1f, 0.2f);
    }
}
