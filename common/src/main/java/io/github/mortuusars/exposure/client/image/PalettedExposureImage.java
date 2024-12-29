package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;

public class PalettedExposureImage extends PalettedImage implements RenderableImage {
    private final String id;

    public PalettedExposureImage(String id, PalettedExposure exposure) {
        super(exposure);
        this.id = exposure.equals(PalettedExposure.EMPTY) ? "empty" : id;
    }

    @Override
    public String getIdentifier() {
        return id;
    }
}
