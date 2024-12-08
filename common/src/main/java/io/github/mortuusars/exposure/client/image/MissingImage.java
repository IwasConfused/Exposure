package io.github.mortuusars.exposure.client.image;


public class MissingImage implements Image {
    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getHeight() {
        return 2;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return (x + y % 2) % 2 == 0 ? 0xFF000000 : 0xFFFF01ED; // black/pink checkerboard
    }
}
