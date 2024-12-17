package io.github.mortuusars.exposure.client.gui.screen.camera;

import io.github.mortuusars.exposure.client.gui.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.core.camera.NewCamera;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class ViewfinderCameraControlsScreen extends Screen {
    private final Viewfinder viewfinder;
    private final NewCamera camera;

    public ViewfinderCameraControlsScreen(Viewfinder viewfinder, NewCamera camera) {
        super(CommonComponents.EMPTY);
        this.viewfinder = viewfinder;
        this.camera = camera;
    }

    public Viewfinder getViewfinder() {
        return viewfinder;
    }

    public NewCamera getCamera() {
        return camera;
    }
}
