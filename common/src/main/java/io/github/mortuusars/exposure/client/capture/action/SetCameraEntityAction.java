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
        // Using field directly instead of Minecraft#setCameraEntity here because it's removing currently active postEffect(shader).
        // Even though postEffect will be disabled anyway for the capture (unless turned off in a config), it is not desirable overall.
        Minecrft.get().cameraEntity = cameraEntity;
    }

    @Override
    public void afterCapture() {
        Minecrft.get().cameraEntity = cameraEntityBeforeCapture;
    }
}
