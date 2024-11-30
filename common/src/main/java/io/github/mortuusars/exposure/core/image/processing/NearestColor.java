package io.github.mortuusars.exposure.core.image.processing;

import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.image.color.Color;
import io.github.mortuusars.exposure.warehouse.PalettedImage;

public class NearestColor {
    public static PalettedImage convert(Image image, ColorPalette palette) {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] indexedPixels = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = image.getPixelARGB(x, y);
                int colorIndex = palette.closestColorIndexTo(new Color(color));
                indexedPixels[x + y * width] = (byte)colorIndex;
            }
        }

        return new PalettedImage(width, height, indexedPixels, palette);
    }
}
