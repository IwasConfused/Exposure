package io.github.mortuusars.exposure.core.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.OldCameraItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class CameraAccessors {
    private static final Map<ResourceLocation, CameraAccessor<? extends Camera<? extends OldCameraItem>>> ACCESSORS = new HashMap<>();

    static CameraAccessor<CameraInHand<OldCameraItem>> MAIN_HAND =
            register(Exposure.resource("main_hand"), CameraAccessor.createInHand(InteractionHand.MAIN_HAND, OldCameraItem.class));
    static CameraAccessor<CameraInHand<OldCameraItem>> OFF_HAND =
            register(Exposure.resource("off_hand"), CameraAccessor.createInHand(InteractionHand.OFF_HAND, OldCameraItem.class));

    public static <C extends Camera<I>, I extends OldCameraItem> CameraAccessor<C> register(ResourceLocation id, CameraAccessor<C> accessor) {
        Preconditions.checkState(!ACCESSORS.containsKey(id), "Camera Accessor with id '%s' is already registered.", id);
        ACCESSORS.put(id, accessor);
        return accessor;
    }

    public static @Nullable CameraAccessor<?> byId(ResourceLocation id) {
        return ACCESSORS.get(id);
    }

    public static ResourceLocation idOf(CameraAccessor<?> accessor) {
        for (Map.Entry<ResourceLocation, CameraAccessor<?>> entry : ACCESSORS.entrySet()) {
            if (entry.getValue() == accessor) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Accessor is not registered.");
    }

    public static CameraAccessor<CameraInHand<OldCameraItem>> ofHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? MAIN_HAND : OFF_HAND;
    }
}
