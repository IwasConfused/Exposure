package io.github.mortuusars.exposure.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class CameraInstances {
    private static final Map<UUID, CameraInstance> INSTANCES = new HashMap<>();

    public static @Nullable CameraInstance get(UUID id) {
        return INSTANCES.get(id);
    }

    public static CameraInstance getOrThrow(UUID id) {
        @Nullable CameraInstance instance = get(id);
        Preconditions.checkState(instance != null, "No Camera Instance with id '%s' found.", id);
        return instance;
    }

    public static void ifPresent(UUID id, Consumer<CameraInstance> instanceConsumer) {
        @Nullable CameraInstance instance = get(id);
        if (instance != null) {
            instanceConsumer.accept(instance);
        }
    }

    public static void ifPresent(ItemStack stack, Consumer<CameraInstance> instanceConsumer) {
        @Nullable UUID id = stack.get(Exposure.DataComponents.CAMERA_ID);
        if (id != null) {
            ifPresent(id, instanceConsumer);
        }
    }

    public static void add(UUID id, CameraInstance instance) {
        INSTANCES.put(id, instance);
    }

    public static void createOrUpdate(UUID id, Consumer<CameraInstance> instanceConsumer) {
        CameraInstance instance = INSTANCES.computeIfAbsent(id, uuid -> new CameraInstance());
        instanceConsumer.accept(instance);
    }
}
