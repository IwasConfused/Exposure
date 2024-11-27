package io.github.mortuusars.exposure.core.image.processing;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.image.color.Color;

public class NearestColor {
    public static byte[] convert(NativeImage image, ColorPalette palette) {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] indexedPixels = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = image.getPixelRGBA(x, y);
                int colorIndex = palette.closestColorIndexTo(new Color(color));
                indexedPixels[x + y * width] = (byte)colorIndex;
            }
        }

        return indexedPixels;
    }
}
