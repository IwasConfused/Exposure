package io.github.mortuusars.exposure.camera.capture.component;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.warehouse.ExposureClientData;
import net.minecraft.nbt.CompoundTag;

public class ExposureUploaderComponent implements ICaptureComponent {
    private final String exposureId;

    public ExposureUploaderComponent(String exposureId) {
        this.exposureId = exposureId;
    }

    @Override
    public boolean save(int width, int height, byte[] pixels, CompoundTag extraData) {
        //TODO: fromFile
        ExposureClientData exposureClientData = new ExposureClientData(width, height, pixels, false, extraData);
        ExposureClient.exposureUploader().uploadToServer(exposureId, exposureClientData);
        return true;
    }
}
