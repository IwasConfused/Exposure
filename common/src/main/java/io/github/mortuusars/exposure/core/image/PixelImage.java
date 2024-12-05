package io.github.mortuusars.exposure.core.image;

public class PixelImage implements Image {
    private final int width;
    private final int height;
    private final int[] pixels;

    public PixelImage(int width, int height, int[] pixels) {
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
