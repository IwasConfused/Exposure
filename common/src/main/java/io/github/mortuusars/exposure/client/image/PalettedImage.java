package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.core.color.ColorPalette;
import io.github.mortuusars.exposure.core.warehouse.ExposureData;

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

    public PalettedImage(ExposureData exposure) {
        this(exposure.getWidth(), exposure.getHeight(), exposure.getPixels(),
                ExposureClient.colorPalettes().getOrDefault(exposure.getPaletteId()));
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
        int id = pixels[y * width + x] & 0xFF;
        return palette.byId(id);
    }
}
