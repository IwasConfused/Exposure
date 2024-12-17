package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.camera.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.core.camera.Camera;
import io.github.mortuusars.exposure.item.CameraItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Viewfinders {
    private static final Map<CameraItem, Viewfinder> VIEWFINDERS = new HashMap<>();

    public static void register(CameraItem item, Viewfinder viewfinder) {
        Preconditions.checkState(!VIEWFINDERS.containsKey(item), "Viewfinder for item '%s' is already registered.", item);
        VIEWFINDERS.put(item, viewfinder);
    }

    public static @Nullable Viewfinder get(CameraItem item) {
        return VIEWFINDERS.get(item);
    }

    public static Viewfinder getOrThrow(CameraItem item) {
        @Nullable Viewfinder viewfinder = VIEWFINDERS.get(item);
        Preconditions.checkNotNull(viewfinder, "No viewfinder for item '%s' is registered.", item);
        return viewfinder;
    }

    static {
        register(Exposure.Items.CAMERA.get(),
                new Viewfinder(ViewfinderOverlay::new, ViewfinderShader::new, ViewfinderCameraControlsScreen::new));
    }

    //TODO: move to CameraClient.setViewfinder()

    public static void setActiveCamera(@Nullable Camera camera) {
        if (camera != null && camera.getItemStack().getItem() instanceof CameraItem cameraItem) {
            activeViewfinder = getOrThrow(cameraItem);
            activeViewfinder.setup(camera);
        } else if (activeViewfinder != null) {
            activeViewfinder.close();
            activeViewfinder = null;
        }
    }

    // --

    private static @Nullable Viewfinder activeViewfinder;

    public static @Nullable Viewfinder active() {
        return activeViewfinder;
    }

    public static Optional<Viewfinder> getActive() {
        return Optional.ofNullable(activeViewfinder);
    }

    public static boolean keyPressed(int key, int scanCode, int action) {
        return activeViewfinder != null && activeViewfinder.keyPressed(key, scanCode, action);
    }

    public static boolean mouseClicked(int button, int action) {
        return activeViewfinder != null && activeViewfinder.mouseClicked(button, action);
    }

    public static void processShader() {
        if (activeViewfinder != null) activeViewfinder.processShader();
    }

    public static void render() {
        if (activeViewfinder != null) activeViewfinder.render();
    }
}
