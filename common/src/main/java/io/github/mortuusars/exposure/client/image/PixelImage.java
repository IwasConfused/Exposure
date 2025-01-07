package io.github.mortuusars.exposure.client.image;

import java.util.function.Function;

public class PixelImage implements Image {
    private final int width;
    private final int height;
    private final int[] pixels;

    public PixelImage(int width, int height, int[] pixels) {
        Image.validate(width, height, pixels.length);
        this.width = width;
        this.height = height;
        this.pixels = pixels;
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
    public int getPixelARGB(int x, int y) {
        return pixels[y * width + x];
    }

    public void setPixelARGB(int x, int y, int color) {
        pixels[y * getWidth() + x] = color;
    }

    public void modifyPixelARGB(int x, int y, Function<Integer, Integer> modifyFunc) {
        pixels[y * getWidth() + x] = modifyFunc.apply(pixels[y * getWidth() + x]);
    }

    public static PixelImage create(int width, int height) {
        return new PixelImage(width, height, new int[width * height]);
    }

    public static PixelImage copyFrom(Image image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixels[y * image.getWidth() + x] = image.getPixelARGB(x, y);
            }
        }

        return new PixelImage(image.getWidth(), image.getHeight(), pixels);
    }
}
