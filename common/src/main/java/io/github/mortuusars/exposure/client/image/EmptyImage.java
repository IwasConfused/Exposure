package io.github.mortuusars.exposure.client.image;

public class EmptyImage implements Image {
    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return 0x00000000;
    }
}
