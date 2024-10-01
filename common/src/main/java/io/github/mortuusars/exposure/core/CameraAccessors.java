package io.github.mortuusars.exposure.core;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CameraAccessors {
    private static final Map<ResourceLocation, CameraAccessor> ACCESSORS = new HashMap<>();

    public static final CameraAccessor MAIN_HAND = register(Exposure.resource("main_hand"),
            new CameraAccessor(entity -> {
                if (entity instanceof LivingEntity livingEntity) {
                    ItemStack itemInHand = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
                    return itemInHand.getItem() instanceof CameraItem
                            ? Optional.of(new Camera(itemInHand, InteractionHand.MAIN_HAND)) : Optional.empty();
                }
                return Optional.empty();
            }));

    public static final CameraAccessor OFF_HAND = register(Exposure.resource("off_hand"),
            new CameraAccessor(entity -> {
                if (entity instanceof LivingEntity livingEntity) {
                    ItemStack itemInHand = livingEntity.getItemInHand(InteractionHand.OFF_HAND);
                    return itemInHand.getItem() instanceof CameraItem
                            ? Optional.of(new Camera(itemInHand, InteractionHand.OFF_HAND)) : Optional.empty();
                }
                return Optional.empty();
            }));

    public static CameraAccessor register(ResourceLocation id, CameraAccessor accessor) {
        Preconditions.checkState(!ACCESSORS.containsKey(id),
                "Camera Accessor with exposureId '{}' is already registered.", id);
        ACCESSORS.put(id, accessor);
        return accessor;
    }

    public static CameraAccessor byId(ResourceLocation id) {
        return ACCESSORS.get(id);
    }

    public static ResourceLocation idOf(CameraAccessor accessor) {
        for (Map.Entry<ResourceLocation, CameraAccessor> entry : ACCESSORS.entrySet()) {
            if (entry.getValue() == accessor) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Accessor is not registered.");
    }

    public static CameraAccessor ofHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? MAIN_HAND : OFF_HAND;
    }
}
