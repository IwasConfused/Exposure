package io.github.mortuusars.exposure.world.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuides;
import io.github.mortuusars.exposure.world.camera.component.FlashMode;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CameraSettings {
    private static final Map<ResourceLocation, CameraSetting<?>> REGISTRY = new HashMap<>();

    public static <T> CameraSetting<T> register(ResourceLocation id, CameraSetting<T> setting) {
        Preconditions.checkArgument(!REGISTRY.containsKey(id), "Setting with id '%s' is already registered.", id);
        REGISTRY.put(id, setting);
        return setting;
    }

    public static CameraSetting<?> byId(ResourceLocation id) {
        @Nullable CameraSetting<?> setting = REGISTRY.get(id);
        if (setting == null) {
            throw new IllegalStateException("Setting with id '" + id + "' is not registered.");
        }
        return setting;
    }

    public static ResourceLocation idOf(CameraSetting<?> setting) {
        for (Map.Entry<ResourceLocation, CameraSetting<?>> entry : REGISTRY.entrySet()) {
            if (entry.getValue().equals(setting)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Setting is not registered.");
    }

    public static final CameraSetting<Boolean> SELFIE_MODE = register(Exposure.resource("selfie"),
            new CameraSetting<>(Exposure.DataComponents.SELFIE_MODE, false));
    public static final CameraSetting<Float> ZOOM = register(Exposure.resource("zoom"),
            new CameraSetting<>(Exposure.DataComponents.ZOOM, 0f));
    public static final CameraSetting<ShutterSpeed> SHUTTER_SPEED = register(Exposure.resource("shutter_speed"),
            new CameraSetting<>(Exposure.DataComponents.SHUTTER_SPEED, ShutterSpeed.DEFAULT));
    public static final CameraSetting<CompositionGuide> COMPOSITION_GUIDE = register(Exposure.resource("composition_guide"),
            new CameraSetting<>(Exposure.DataComponents.COMPOSITION_GUIDE, CompositionGuides.NONE));
    public static final CameraSetting<FlashMode> FLASH_MODE = register(Exposure.resource("flash_mode"),
            new CameraSetting<>(Exposure.DataComponents.FLASH_MODE, FlashMode.OFF));
}
