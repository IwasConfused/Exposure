package io.github.mortuusars.exposure.client.snapshot.capturing.action;

import io.github.mortuusars.exposure.client.Minecrft;
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
        Minecrft.get().setCameraEntity(cameraEntity);
    }

    @Override
    public void afterCapture() {
        Minecrft.get().setCameraEntity(cameraEntityBeforeCapture);
    }
}
