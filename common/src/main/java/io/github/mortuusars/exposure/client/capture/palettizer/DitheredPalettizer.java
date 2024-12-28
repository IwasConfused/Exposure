package io.github.mortuusars.exposure.client.capture.palettizer;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.image.color.Color;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.client.image.PalettedImage;

/**
 * Floyd-Steinberg dithering algorithm.
 * <br>Credit:
 * <a href="http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering">stackoverflow post</a>
 */
public class DitheredPalettizer implements ImagePalettizer {
    @Override
    public PalettedImage palettize(Image image, ColorPalette palette) {
        return palettize(getPixels(image), palette);
    }

    public PalettedImage palettize(Color[][] pixels, ColorPalette palette) {
        int width = pixels[0].length;
        int height = pixels.length;

        byte[] indexedPixels = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color oldColor = pixels[y][x];
                int colorIndex = palette.closestTo(oldColor);

                indexedPixels[y * width + x] = (byte)colorIndex;

                Color newColor = palette.byIndex(colorIndex);

                Color.Unbounded error = oldColor.subtractUnbounded(newColor);

                if (x + 1 < width) {
                    pixels[y][x + 1] = applyError(pixels[y][x + 1], error, 7. / 16);
                }

                if (x - 1 >= 0 && y + 1 < height) {
                    pixels[y + 1][x - 1] = applyError(pixels[y + 1][x - 1], error, 3. / 16);
                }

                if (y + 1 < height) {
                    pixels[y + 1][x] = applyError(pixels[y + 1][x], error, 5. / 16);
                }

                if (x + 1 < width && y + 1 < height) {
                    pixels[y + 1][x + 1] = applyError(pixels[y + 1][x + 1], error, 1. / 16);
                }
            }
        }

        return new PalettedImage(width, height, indexedPixels, palette);
    }

    private Color applyError(Color color, Color.Unbounded error, double scalar) {
        return color.addUnbounded(error.multiply(scalar)).clamp();
    }

    private Color[][] getPixels(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Color[][] pixels = new Color[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = Color.argb(image.getPixelARGB(x, y));
            }
        }

        return pixels;
    }
}