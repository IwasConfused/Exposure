package io.github.mortuusars.exposure.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class CameraInstances {
    private static final Map<CameraID, CameraInstance> INSTANCES = new HashMap<>();

    public static @Nullable CameraInstance get(CameraID id) {
        if (id.uuid().equals(Util.NIL_UUID)) return null;
        return INSTANCES.get(id);
    }

    public static Optional<CameraInstance> getOptional(CameraID id) {
        if (id.uuid().equals(Util.NIL_UUID)) return Optional.empty();
        return Optional.ofNullable(INSTANCES.get(id));
    }

    public static @Nullable CameraInstance get(ItemStack stack) {
        return get(CameraID.ofStack(stack));
    }

    public static Optional<CameraInstance> getOptional(ItemStack stack) {
        return getOptional(CameraID.ofStack(stack));
    }

    public static CameraInstance getOrThrow(CameraID id) {
        @Nullable CameraInstance instance = get(id);
        Preconditions.checkState(instance != null, "No Camera Instance with id '%s' found.", id);
        return instance;
    }

    public static void ifPresent(CameraID id, Consumer<CameraInstance> instanceConsumer) {
        @Nullable CameraInstance instance = get(id);
        if (instance != null) {
            instanceConsumer.accept(instance);
        }
    }

    public static void ifPresent(ItemStack stack, Consumer<CameraInstance> instanceConsumer) {
        @Nullable CameraID id = stack.get(Exposure.DataComponents.CAMERA_ID);
        if (id != null) {
            ifPresent(id, instanceConsumer);
        }
    }

    public static void add(CameraID id, CameraInstance instance) {
        INSTANCES.put(id, instance);
    }

    public static void createOrUpdate(CameraID id, Consumer<CameraInstance> instanceConsumer) {
        CameraInstance instance = INSTANCES.computeIfAbsent(id, uuid -> new CameraInstance(id));
        instanceConsumer.accept(instance);
    }

    // --

    public static boolean canReleaseShutter(CameraID id) {
        return getOptional(id).map(CameraInstance::canReleaseShutter).orElse(true);
    }
}
