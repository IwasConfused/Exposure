package io.github.mortuusars.exposure.core.camera;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.item.OldCameraItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public interface CameraAccessor<C extends Camera<? extends OldCameraItem>> {
    Codec<CameraAccessor<?>> CODEC = ResourceLocation.CODEC.xmap(CameraAccessors::byId, CameraAccessors::idOf);

    StreamCodec<ByteBuf, CameraAccessor<?>> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CameraAccessors::idOf,
            CameraAccessors::byId
    );

    @Nullable C get(Entity entity);

    default C getOrThrow(Entity entity) {
        @Nullable C camera = get(entity);
        Preconditions.checkNotNull(camera, "Unable to get the camera from accessor '%s'", this);
        return camera;
    }

    default Optional<C> ifPresent(Entity entity, Consumer<C> consumer) {
        @Nullable C camera = get(entity);
        if (camera != null) {
            consumer.accept(camera);
        }
        return Optional.ofNullable(camera);
    }

    default Optional<C> ifPresentOrElse(Entity entity, Consumer<C> consumer, Runnable runnable) {
        @Nullable C camera = get(entity);
        if (camera != null) {
            consumer.accept(camera);
        }
        else {
            runnable.run();
        }
        return Optional.ofNullable(camera);
    }

    default Optional<CameraInHand<?>> ifInHand(Entity entity) {
        @Nullable C camera = get(entity);
        if (camera instanceof CameraInHand<?> cameraInHand) {
            return Optional.of(cameraInHand);
        }
        return Optional.empty();
    }

    default <T extends OldCameraItem> Optional<CameraInHand<T>> ifInHandOfType(Entity entity, Class<T> clazz) {
        @Nullable C camera = get(entity);
        if (camera instanceof CameraInHand<?> cameraInHand && clazz.isInstance(cameraInHand.getItem())) {
            //noinspection unchecked
            return Optional.of((CameraInHand<T>) cameraInHand);
        }
        return Optional.empty();
    }

    static <T extends OldCameraItem> CameraAccessor<@Nullable CameraInHand<T>> createInHand(InteractionHand hand, Class<T> clazz) {
        return entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack stack = livingEntity.getItemInHand(hand);
                return clazz.isInstance(stack.getItem()) ? new CameraInHand<>(stack, hand) : null;
            }
            return null;
        };
    }
}