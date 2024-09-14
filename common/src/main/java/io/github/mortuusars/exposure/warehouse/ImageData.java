package io.github.mortuusars.exposure.warehouse;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;

public record ImageData(int width, int height, byte[] pixels) { //TODO: palette
    public static final ImageData EMPTY = new ImageData(1, 1, new byte[]{0});

    public ImageData {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(height >= 0, "Height cannot be negative.");
        if (pixels.length != width * height) {
            Exposure.LOGGER.warn("Pixel count '{}' is not correct for image dimensions of '{}x{}'. " +
                    "Should be '{}pixels'", pixels.length, width, height, width * height);
        }
    }
}
