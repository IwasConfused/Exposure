package io.github.mortuusars.exposure.warehouse;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.core.image.color.Color;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;

public record PalettizedImage(int width, int height, byte[] pixels, ColorPalette palette) implements Image {
//    public static final PalettedImage EMPTY = new PalettedImage(1, 1, new byte[]{0}, ColorPalette.MAP_COLORS);

    public PalettizedImage {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative. %s", this);
        Preconditions.checkArgument(height >= 0, "Height cannot be negative. %s ", this);
        Preconditions.checkArgument(pixels.length == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixels.length, width, height, width * height);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public int getPixelARGB(int x, int y) {
        return getPixelColorARGB(x, y).getRGB();
    }

    public Color getPixelColorARGB(int x, int y) {
        int colorIndex = pixels[y * width + x] & 0xFF;
        return palette.byIndex(colorIndex);
    }
}
