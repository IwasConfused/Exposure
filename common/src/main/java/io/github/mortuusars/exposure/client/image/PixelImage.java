package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;

import java.util.function.Function;

public class PixelImage implements Image {
    private final int width;
    private final int height;
    private final int[] pixels;

    public PixelImage(int width, int height, int[] pixels) {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative. %s", this);
        Preconditions.checkArgument(height >= 0, "Height cannot be negative. %s ", this);
        Preconditions.checkArgument(pixels.length == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixels.length, width, height, width * height);
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
