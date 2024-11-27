package io.github.mortuusars.exposure.core.image.processing;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.image.color.Color;

/**
 * Floyd-Steinberg dithering algorithm.
 * <br>Credit:
 * <a href="http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering">stackoverflow post</a>
 */
public class Dithering {
    /**
     * @return New instance of NativeImage. Original image is not modified.
     */
    public static NativeImage dither(NativeImage image, ColorPalette palette) {
        Color[] paletteColors = palette.getColors();

        int width = image.getWidth();
        int height = image.getHeight();

        Color[][] pixels = getPixels(image);

        NativeImage resultImage = new NativeImage(width, height, false);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color oldColor = pixels[y][x];
                Color newColor = paletteColors[palette.closestColorIndexTo(oldColor)];

                resultImage.setPixelRGBA(x, y, newColor.clamp(0, 255).getRGB());

                Color err = oldColor.subtract(newColor);

                if (x + 1 < width) {
                    pixels[y][x + 1] = pixels[y][x + 1].add(err.scalarMultiply(7. / 16));
                }

                if (x - 1 >= 0 && y + 1 < height) {
                    pixels[y + 1][x - 1] = pixels[y + 1][x - 1].add(err.scalarMultiply(3. / 16));
                }

                if (y + 1 < height) {
                    pixels[y + 1][x] = pixels[y + 1][x].add(err.scalarMultiply(5. / 16));
                }

                if (x + 1 < width && y + 1 < height) {
                    pixels[y + 1][x + 1] = pixels[y + 1][x + 1].add(err.scalarMultiply(1. / 16));
                }
            }
        }

        return resultImage;
    }

    public static byte[] ditherIndexed(NativeImage image, ColorPalette palette) {
        return ditherIndexed(getPixels(image), palette);
    }

    public static byte[] ditherIndexed(Color[][] pixels, ColorPalette palette) {
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
                    pixels[y][x + 1] = pixels[y][x + 1].add(error.scalarMultiply(7. / 16)).clamp(0, 255);
                }

                if (x - 1 >= 0 && y + 1 < height) {
                    pixels[y + 1][x - 1] = pixels[y + 1][x - 1].add(error.scalarMultiply(3. / 16)).clamp(0, 255);
                }

                if (y + 1 < height) {
                    pixels[y + 1][x] = pixels[y + 1][x].add(error.scalarMultiply(5. / 16));
                }

                if (x + 1 < width && y + 1 < height) {
                    pixels[y + 1][x + 1] = pixels[y + 1][x + 1].add(error.scalarMultiply(1. / 16)).clamp(0, 255);
                }
            }
        }

        return indexedPixels;
    }

    public static Color[][] getPixels(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Color[][] pixels = new Color[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = new Color(image.getPixelRGBA(x, y));
            }
        }

        return pixels;
    }
}