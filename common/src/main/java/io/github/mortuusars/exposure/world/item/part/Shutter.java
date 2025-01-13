package io.github.mortuusars.exposure.world.item.part;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.item.component.camera.ShutterState;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.apache.logging.log4j.util.TriConsumer;

public class Shutter {
    protected TriConsumer<PhotographerEntity, ServerLevel, ItemStack> onOpen = (entity, level, stack) -> { };
    protected TriConsumer<PhotographerEntity, ServerLevel, ItemStack> onClosed = (entity, level, stack) -> { };

    public void onOpen(TriConsumer<PhotographerEntity, ServerLevel, ItemStack> onOpen) {
        this.onOpen = onOpen;
    }

    /**
     * Will not be executed when closing time should've been "long" ago.
     * When camera wasn't in inventory at the time of closing, for example.
     */
    public void onClosed(TriConsumer<PhotographerEntity, ServerLevel, ItemStack> onClosed) {
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
        boolean projecting = CameraInstances.getOptional(stack).map(CameraInstance::isWaitingForProjection).orElse(false);
        return state.isOpen() && !projecting && gameTime >= state.getCloseTick();
    }

    public void tick(PhotographerEntity photographer, ServerLevel level, ItemStack stack) {
        long gameTime = photographer.asEntity().level().getGameTime();
        if (shouldClose(stack, gameTime)) {
            ShutterState state = getState(stack);
            if (gameTime - state.getCloseTick() > 200) {
                setState(stack, ShutterState.CLOSED);
            } else {
                close(photographer, level, stack);
            }
        }
    }

    public void open(PhotographerEntity photographer, ServerLevel level, ItemStack stack, ShutterSpeed shutterSpeed) {
        setState(stack, ShutterState.open(photographer.asEntity().level().getGameTime(), shutterSpeed));
        photographer.asEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playOpenSound(photographer);
        onOpen.accept(photographer, level, stack);
    }

    public void close(PhotographerEntity photographer, ServerLevel level, ItemStack stack) {
        setState(stack, ShutterState.closed());
        photographer.asEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playCloseSound(photographer);
        onClosed.accept(photographer, level, stack);
    }

    public void playOpenSound(PhotographerEntity photographer) {
        photographer.playCameraSound(Exposure.SoundEvents.SHUTTER_OPEN.get(), 0.7f, 1.1f, 0.2f);
    }

    public void playCloseSound(PhotographerEntity photographer) {
        photographer.playCameraSound(Exposure.SoundEvents.SHUTTER_CLOSE.get(), 0.7f, 1.1f, 0.2f);
    }
}
