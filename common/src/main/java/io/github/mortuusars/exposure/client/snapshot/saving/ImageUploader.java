package io.github.mortuusars.exposure.client.snapshot.saving;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ExposureClientDataC2SP;
import io.github.mortuusars.exposure.warehouse.ExposureClientData;
import io.github.mortuusars.exposure.client.image.PalettizedImage;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

public class ImageUploader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ExposureIdentifier identifier;

    public ImageUploader(ExposureIdentifier identifier) {
        this.identifier = identifier;
    }

    public void upload(PalettizedImage image) {
        //TODO: from file
        ExposureClientData exposureClientData = new ExposureClientData(image.getWidth(), image.getHeight(),
                image.pixels(), image.palette(), false, new CompoundTag());

        LOGGER.debug("Sending exposure '{}' to server...", identifier);

        Packets.sendToServer(new ExposureClientDataC2SP(identifier, exposureClientData));

//        ExposureClient.exposureUploader().uploadToServer(identifier, exposureClientData);
    }
}
