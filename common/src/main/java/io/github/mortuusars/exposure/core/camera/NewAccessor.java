package io.github.mortuusars.exposure.core.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface NewAccessor<C extends NewCamera<? extends CameraItem>> {
    NewAccessor<NewCameraInHand<CameraItem>> MAIN_HAND = createInHand(InteractionHand.MAIN_HAND, CameraItem.class);
    NewAccessor<NewCameraInHand<CameraItem>> OFF_HAND = createInHand(InteractionHand.OFF_HAND, CameraItem.class);

    @Nullable C get(Entity entity);

    default C getOrThrow(Entity entity) {
        @Nullable C camera = get(entity);
        Preconditions.checkNotNull(camera, "Unable to get the camera from accessor '%s'", this);
        return camera;
    }

    default Optional<C> ifPresent(Entity entity) {
        return Optional.ofNullable(get(entity));
    }

    default Optional<NewCameraInHand<?>> ifInHand(Entity entity) {
        @Nullable C camera = get(entity);
        if (camera instanceof NewCameraInHand<?> cameraInHand) {
            return Optional.of(cameraInHand);
        }
        return Optional.empty();
    }

    default <T extends CameraItem> Optional<NewCameraInHand<T>> ifInHandOfType(Entity entity, Class<T> clazz) {
        @Nullable C camera = get(entity);
        if (camera instanceof NewCameraInHand<?> cameraInHand && clazz.isInstance(cameraInHand.getItem())) {
            //noinspection unchecked
            return Optional.of((NewCameraInHand<T>) cameraInHand);
        }
        return Optional.empty();
    }

    static <T extends CameraItem> NewAccessor<@Nullable NewCameraInHand<T>> createInHand(InteractionHand hand, Class<T> clazz) {
        return entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack stack = livingEntity.getItemInHand(hand);
                return clazz.isInstance(stack.getItem()) ? new NewCameraInHand<>(stack, hand) : null;
            }
            return null;
        };
    }
}