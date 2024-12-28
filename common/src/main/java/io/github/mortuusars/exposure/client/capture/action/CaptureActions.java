package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.Entity;

public interface CaptureActions {
    static CaptureAction forceCamera(CameraType cameraType) {
        return new ForceCameraTypeAction(cameraType);
    }

    static CaptureAction forceRegularOrSelfieCamera() {
        return new ForceRegularOrSelfieCameraTypeAction();
    }

    static CaptureAction hideGui() {
        return new HideGuiAction();
    }

    static CaptureAction disablePostEffect() {
        return new DisablePostEffectAction();
    }

    static CaptureAction modifyGamma(float brightnessStops) {
        return brightnessStops != 0 ? new ModifyGammaAction(brightnessStops) : CaptureAction.EMPTY;
    }

    static CaptureAction flash(Entity photographer) {
        return new FlashAction(photographer);
    }

    static CaptureAction interplanarProjection(PhotographerEntity photographer, CameraID cameraID) {
        return new InterplanarProjectionAction(photographer, cameraID);
    }

    static CaptureAction setCameraEntity(Entity viewEntity) {
        return new SetCameraEntityAction(viewEntity);
    }

    static CaptureAction setFov(double fov) {
        return new SetFovAction(fov);
    }
}
