package io.github.mortuusars.exposure.client.snapshot.saving;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.warehouse.ExposureClientData;
import io.github.mortuusars.exposure.warehouse.PalettedImage;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

public class ImageUploader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String exposureId;

    public ImageUploader(String exposureId) {
        this.exposureId = exposureId;
    }

    public void upload(PalettedImage image) {
        ExposureClientData exposureClientData = new ExposureClientData(image.getWidth(), image.getHeight(), image.pixels(), false, new CompoundTag());
        LOGGER.info("Sending exposure '{}' to server.", exposureId);
        ExposureClient.exposureUploader().uploadToServer(exposureId, exposureClientData);
    }
}
