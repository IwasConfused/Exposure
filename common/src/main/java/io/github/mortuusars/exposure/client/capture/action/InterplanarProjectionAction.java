package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.InterplanarProjectionFinishedC2SP;
import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.Optional;

public class InterplanarProjectionAction implements CaptureAction {
    private final CameraID cameraID;

    public InterplanarProjectionAction(CameraID cameraID) {
        this.cameraID = cameraID;
    }

    @Override
    public void onSuccess() {
        Packets.sendToServer(new InterplanarProjectionFinishedC2SP(cameraID, true, Optional.empty()));
    }

    @Override
    public void onFailure(TranslatableError error) {
        Packets.sendToServer(new InterplanarProjectionFinishedC2SP(cameraID, false, Optional.of(error)));
    }
}
