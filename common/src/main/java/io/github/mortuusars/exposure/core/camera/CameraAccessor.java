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

public interface CameraAccessor<C extends OtherCamera<? extends OldCameraItem>> {
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

    default Optional<OtherCameraInHand<?>> ifInHand(Entity entity) {
        @Nullable C camera = get(entity);
        if (camera instanceof OtherCameraInHand<?> cameraInHand) {
            return Optional.of(cameraInHand);
        }
        return Optional.empty();
    }

    default <T extends OldCameraItem> Optional<OtherCameraInHand<T>> ifInHandOfType(Entity entity, Class<T> clazz) {
        @Nullable C camera = get(entity);
        if (camera instanceof OtherCameraInHand<?> cameraInHand && clazz.isInstance(cameraInHand.getItem())) {
            //noinspection unchecked
            return Optional.of((OtherCameraInHand<T>) cameraInHand);
        }
        return Optional.empty();
    }

    static <T extends OldCameraItem> CameraAccessor<@Nullable OtherCameraInHand<T>> createInHand(InteractionHand hand, Class<T> clazz) {
        return entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack stack = livingEntity.getItemInHand(hand);
                return clazz.isInstance(stack.getItem()) ? new OtherCameraInHand<>(stack, hand) : null;
            }
            return null;
        };
    }
}