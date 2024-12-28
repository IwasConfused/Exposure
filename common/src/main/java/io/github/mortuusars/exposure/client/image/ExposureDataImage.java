package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.foundation.warehouse.ExposureData;

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

    @Override
    public boolean isEmpty() {
        return exposureData.equals(ExposureData.EMPTY) || Image.super.isEmpty();
    }
}
