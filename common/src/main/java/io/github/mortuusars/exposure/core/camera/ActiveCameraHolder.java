package io.github.mortuusars.exposure.core.camera;

import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * This interface is injected to a Player class in {@link io.github.mortuusars.exposure.mixin.PlayerMixin} to add camera-related methods.
 * It also must be defined in 'architectury.common.json' with 'injected_interfaces' to work.
 * It could also be used in the future for custom entities or injected to other vanilla entities.
 */
public interface ActiveCameraHolder {
    default @Nullable Camera activeExposureCamera() {
        return null;
    }

    default Optional<Camera> getActiveExposureCamera() {
        return Optional.ofNullable(activeExposureCamera());
    }

    default void ifActiveExposureCameraPresent(BiConsumer<CameraItem, ItemStack> ifPresent) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null) {
            camera.ifPresent(ifPresent);
        }
    }

    default void ifActiveExposureCameraPresent(BiConsumer<CameraItem, ItemStack> ifPresent, Runnable orElse) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null) {
            camera.ifPresent(ifPresent);
        } else {
            orElse.run();
        }
    }

    default <T> T mapActiveExposureCamera(BiFunction<CameraItem, ItemStack, T> map, T orElse) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null) {
            return camera.map(map, orElse);
        } else {
            return orElse;
        }
    }

    default void mapActiveExposureCamera(BiConsumer<CameraItem, ItemStack> ifPresent, Runnable orElse) {
        @Nullable Camera camera = activeExposureCamera();
        if (camera != null) {
            camera.ifPresent(ifPresent);
        } else {
            orElse.run();
        }
    }

    default void setActiveExposureCamera(@Nullable Camera camera) {
    }

    default void removeActiveExposureCamera() {
    }

    default boolean activeExposureCameraMatches(ItemStack stack) {
        return mapActiveExposureCamera((item, cStack) -> cStack.equals(stack), false);
    }
}