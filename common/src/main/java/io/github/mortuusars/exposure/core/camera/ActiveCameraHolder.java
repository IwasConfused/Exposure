package io.github.mortuusars.exposure.core.camera;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This interface is injected to a Player class in {@link io.github.mortuusars.exposure.mixin.PlayerMixin} to add camera-related methods.
 * It also must be defined in 'architectury.common.json' with 'injected_interfaces' to work.
 * It could also be used in the future for custom entities or injected to other vanilla entities.
 */
public interface ActiveCameraHolder {
    default Optional<NewCamera> getActiveCamera() {
        return Optional.empty();
    }

    default void setActiveCamera(@Nullable NewCamera camera) {
    }

    default void removeActiveCamera() {
    }

    default boolean activeCameraMatches(ItemStack stack) {
        return getActiveCamera().map(camera -> camera.getItemStack().equals(stack)).orElse(false);
    }
}