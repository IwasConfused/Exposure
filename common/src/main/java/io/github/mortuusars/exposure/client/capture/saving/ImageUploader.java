package io.github.mortuusars.exposure.client.capture.saving;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ExposureClientDataC2SP;
import io.github.mortuusars.exposure.core.warehouse.CapturedExposure;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import org.slf4j.Logger;

public class ImageUploader {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final ExposureIdentifier identifier;
    protected final boolean fromFile;

    public ImageUploader(ExposureIdentifier identifier, boolean fromFile) {
        this.identifier = identifier;
        this.fromFile = fromFile;
    }

    public void upload(PalettedImage image) {
        CapturedExposure capturedExposure = new CapturedExposure(
                image.getWidth(), image.getHeight(), image.pixels(), image.palette(), fromFile);

        LOGGER.debug("Sending exposure '{}' to server...", identifier);

        Packets.sendToServer(new ExposureClientDataC2SP(identifier, capturedExposure));
    }
}
