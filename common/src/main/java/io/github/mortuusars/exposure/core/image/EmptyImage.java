package io.github.mortuusars.exposure.core.image;

public class EmptyImage implements IdentifiableImage {
    @Override
    public String id() {
        return "empty_image";
    }

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
