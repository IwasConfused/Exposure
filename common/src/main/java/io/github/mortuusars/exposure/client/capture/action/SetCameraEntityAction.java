package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.world.entity.Entity;

public class SetCameraEntityAction implements CaptureAction {
    private final Entity cameraEntity;
    private Entity cameraEntityBeforeCapture;

    public SetCameraEntityAction(Entity cameraEntity) {
        this.cameraEntity = cameraEntity;
        this.cameraEntityBeforeCapture = Minecrft.player();
    }

    @Override
    public void beforeCapture() {
        cameraEntityBeforeCapture = Minecrft.get().getCameraEntity();
        // Not using Minecraft#setCameraEntity because it updates postEffect.
        Minecrft.get().cameraEntity = cameraEntity;
    }

    @Override
    public void afterCapture() {
        Minecrft.get().cameraEntity = cameraEntityBeforeCapture;
    }
}
