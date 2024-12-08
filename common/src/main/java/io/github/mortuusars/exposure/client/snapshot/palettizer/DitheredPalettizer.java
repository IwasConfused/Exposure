package io.github.mortuusars.exposure.client.snapshot.palettizer;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.image.color.Color;
import io.github.mortuusars.exposure.warehouse.PalettizedImage;

/**
 * Floyd-Steinberg dithering algorithm.
 * <br>Credit:
 * <a href="http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering">stackoverflow post</a>
 */
public class DitheredPalettizer implements ImagePalettizer {
    @Override
    public PalettizedImage palettize(Image image, ColorPalette palette) {
        return palettize(getPixels(image), palette);
    }

    public PalettizedImage palettize(Color[][] pixels, ColorPalette palette) {
        int width = pixels[0].length;
        int height = pixels.length;

        byte[] indexedPixels = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color oldColor = pixels[y][x];
                int colorIndex = palette.closestColorIndexTo(oldColor);

                indexedPixels[y * width + x] = (byte)colorIndex;

                Color newColor = palette.byIndex(colorIndex);

                Color error = oldColor.subtract(newColor);

                if (x + 1 < width) {
                    pixels[y][x + 1] = pixels[y][x + 1].add(error.multiply(7. / 16)).clamp(0, 255);
                }

                if (x - 1 >= 0 && y + 1 < height) {
                    pixels[y + 1][x - 1] = pixels[y + 1][x - 1].add(error.multiply(3. / 16)).clamp(0, 255);
                }

                if (y + 1 < height) {
                    pixels[y + 1][x] = pixels[y + 1][x].add(error.multiply(5. / 16)).clamp(0, 255);
                }

                if (x + 1 < width && y + 1 < height) {
                    pixels[y + 1][x + 1] = pixels[y + 1][x + 1].add(error.multiply(1. / 16)).clamp(0, 255);
                }
            }
        }

        return new PalettizedImage(width, height, indexedPixels, palette);
    }

    private Color[][] getPixels(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Color[][] pixels = new Color[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = new Color(image.getPixelARGB(x, y));
            }
        }

        return pixels;
    }
}