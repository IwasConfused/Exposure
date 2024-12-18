package io.github.mortuusars.exposure.client.gui.screen.camera;

import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.core.camera.Camera;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class ViewfinderCameraControlsScreen extends Screen {
    private final Camera camera;
    private final Viewfinder viewfinder;

    public ViewfinderCameraControlsScreen(Camera camera, Viewfinder viewfinder) {
        super(CommonComponents.EMPTY);
        this.camera = camera;
        this.viewfinder = viewfinder;
    }

    public Camera getCamera() {
        return camera;
    }

    public Viewfinder getViewfinder() {
        return viewfinder;
    }
}
