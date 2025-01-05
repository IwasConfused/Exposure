package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.core.color.Color;
import io.github.mortuusars.exposure.core.color.ColorPalette;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;

public class PalettedImage implements Image {
    private final int width;
    private final int height;
    private final byte[] pixels;
    private final ColorPalette palette;

    public PalettedImage(int width, int height, byte[] pixels, ColorPalette palette) {
        Image.validate(width, height, pixels.length);
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.palette = palette;
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

    public byte[] getPixels() {
        return pixels;
    }

    public ColorPalette getPalette() {
        return palette;
    }

    public int getPixelARGB(int x, int y) {
        return getPixelColorARGB(x, y).getARGB();
    }

    public Color getPixelColorARGB(int x, int y) {
        int colorIndex = pixels[y * width + x] & 0xFF;
        return palette.byIndex(colorIndex);
    }
}
