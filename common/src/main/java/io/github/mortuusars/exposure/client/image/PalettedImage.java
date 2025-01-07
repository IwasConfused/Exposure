package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.core.color.ColorPalette;

public record PalettedImage(int getWidth, int getHeight, byte[] pixels, ColorPalette palette) implements Image {
    public PalettedImage {
        Image.validate(getWidth, getHeight, pixels.length);
    }

    public int getPixelARGB(int x, int y) {
        int id = pixels[y * getWidth + x] & 0xFF;
        return palette.byId(id);
    }
}
