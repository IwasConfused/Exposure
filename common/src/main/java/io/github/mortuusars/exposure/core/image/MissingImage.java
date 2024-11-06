package io.github.mortuusars.exposure.core.image;

public class MissingImage implements Image {
    @Override
    public String id() {
        return "missing_image";
    }

    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getHeight() {
        return 2;
    }

    @Override
    public int getPixelABGR(int x, int y) {
        return (x + y % 2) % 2 == 0 ? 0xFF000000 : 0xFFFF01ED; // black/pink checkerboard
    }
}
