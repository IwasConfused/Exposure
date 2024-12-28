package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.image.color.Color;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;

public record PalettedImage(int width, int height, byte[] pixels, ColorPalette palette) implements Image {
    public PalettedImage {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative. %s", this);
        Preconditions.checkArgument(height >= 0, "Height cannot be negative. %s ", this);
        Preconditions.checkArgument(pixels.length == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixels.length, width, height, width * height);
    }

    public PalettedImage(PalettedExposure exposure) {
        this(exposure.getWidth(), exposure.getHeight(), exposure.getPixels(), exposure.getPalette());
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
