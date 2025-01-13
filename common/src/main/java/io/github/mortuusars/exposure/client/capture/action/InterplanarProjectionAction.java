package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.InterplanarProjectionFinishedC2SP;
import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.Optional;

public class InterplanarProjectionAction implements CaptureAction {
    private final PhotographerEntity photographer;
    private final CameraID cameraID;

    public InterplanarProjectionAction(PhotographerEntity photographer, CameraID cameraID) {
        this.photographer = photographer;
        this.cameraID = cameraID;
    }

    @Override
    public void onSuccess() {
        Packets.sendToServer(new InterplanarProjectionFinishedC2SP(photographer.asEntity().getUUID(),
                cameraID, true, Optional.empty()));
    }

    @Override
    public void onFailure(TranslatableError error) {
        Packets.sendToServer(new InterplanarProjectionFinishedC2SP(photographer.asEntity().getUUID(),
                cameraID, false, Optional.of(error)));
    }
}
