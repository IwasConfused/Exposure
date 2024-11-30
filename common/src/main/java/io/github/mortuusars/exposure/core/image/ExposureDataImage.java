package io.github.mortuusars.exposure.core.image;

import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import org.jetbrains.annotations.NotNull;

public record ExposureDataImage(String id, ExposureData exposureData) implements IdentifiableImage {
    public ExposureDataImage(String id, @NotNull ExposureData exposureData) {
        this.id = id;
        this.exposureData = exposureData;
    }

    public int getWidth() {
        return exposureData.getWidth();
    }

    public int getHeight() {
        return exposureData.getHeight();
    }

    public int getPixelARGB(int x, int y) {
        //TODO: custom palette
        return ColorPalette.MAP_COLORS.byIndex(exposureData.getPixel(x, y)).getRGB();
    }
}
