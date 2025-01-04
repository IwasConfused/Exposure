package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.item.CameraItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ViewfinderRegistry {
    private static final Map<CameraItem, Function<Camera, Viewfinder>> VIEWFINDERS = new HashMap<>();

    public static void register(CameraItem item, Function<Camera, Viewfinder> viewfinder) {
        Preconditions.checkState(!VIEWFINDERS.containsKey(item), "Viewfinder for item '%s' is already registered.", item);
        VIEWFINDERS.put(item, viewfinder);
    }

    public static @Nullable Function<Camera, Viewfinder> get(CameraItem item) {
        return VIEWFINDERS.get(item);
    }

    public static Function<Camera, Viewfinder> getOrThrow(CameraItem item) {
        @Nullable Function<Camera, Viewfinder> viewfinder = VIEWFINDERS.get(item);
        Preconditions.checkNotNull(viewfinder, "No viewfinder for item '%s' is registered.", item);
        return viewfinder;
    }
}
