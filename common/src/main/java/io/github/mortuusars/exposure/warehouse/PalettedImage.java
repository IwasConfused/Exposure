package io.github.mortuusars.exposure.warehouse;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.image.color.Color;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;

public record PalettedImage(int width, int height, byte[] pixels, ColorPalette palette) {
    public static final PalettedImage EMPTY = new PalettedImage(1, 1, new byte[]{0}, ColorPalette.MAP_COLORS);

    public PalettedImage {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(height >= 0, "Height cannot be negative.");
        if (pixels.length != width * height) {
            Exposure.LOGGER.warn("Pixel count '{}' is not correct for image dimensions of '{}x{}'. " +
                    "Count should be '{}'.", pixels.length, width, height, width * height);
        }
    }

    public Color getPixelARGB(int x, int y) {
        int colorIndex = pixels[y * width + x] & 0xFF;
        return palette.byIndex(colorIndex);
    }
}
