package io.github.mortuusars.exposure.client.capture.palettizer;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.image.color.Color;
import io.github.mortuusars.exposure.client.image.PalettedImage;

public class NearestColorPalettizer implements ImagePalettizer {
    @Override
    public PalettedImage palettize(Image image, ColorPalette palette) {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] indexedPixels = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = image.getPixelARGB(x, y);
                int colorIndex = palette.closestTo(Color.argb(color));
                indexedPixels[x + y * width] = (byte)colorIndex;
            }
        }

        return new PalettedImage(width, height, indexedPixels, palette);
    }
}
