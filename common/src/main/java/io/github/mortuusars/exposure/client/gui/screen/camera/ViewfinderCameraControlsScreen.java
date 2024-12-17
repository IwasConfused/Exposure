package io.github.mortuusars.exposure.client.gui.screen.camera;

import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.core.camera.Camera;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class ViewfinderCameraControlsScreen extends Screen {
    private final Viewfinder viewfinder;
    private final Camera camera;

    public ViewfinderCameraControlsScreen(Viewfinder viewfinder, Camera camera) {
        super(CommonComponents.EMPTY);
        this.viewfinder = viewfinder;
        this.camera = camera;
    }

    public Viewfinder getViewfinder() {
        return viewfinder;
    }

    public Camera getCamera() {
        return camera;
    }
}
