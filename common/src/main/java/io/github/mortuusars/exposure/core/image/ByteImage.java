package io.github.mortuusars.exposure.core.image;

public class ByteImage implements IImage {
    private final String id;
    private final int width;
    private final int height;
    private final byte[] pixels;

    public ByteImage(String id, int width, int height, byte[] pixels) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public ByteImage(String id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.pixels = new byte[width * height];
    }

    @Override
    public String getImageId() {
        return id;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getPixelABGR(int x, int y) {
        return pixels[y * width + x];
    }
}
