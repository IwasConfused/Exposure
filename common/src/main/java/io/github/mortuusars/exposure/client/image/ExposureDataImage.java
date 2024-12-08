package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.warehouse.ExposureData;

public record ExposureDataImage(ExposureData exposureData) implements Image {
    public int getWidth() {
        return exposureData.getWidth();
    }

    public int getHeight() {
        return exposureData.getHeight();
    }

    public int getPixelARGB(int x, int y) {
        return exposureData.getPalette().byIndex(exposureData.getPixel(x, y)).getRGB();
    }

    public static ExposureDataImage getOrQuery(ExposureIdentifier identifier) {
        return new ExposureDataImage(ExposureClient.getOrQuery(identifier));
    }
}
