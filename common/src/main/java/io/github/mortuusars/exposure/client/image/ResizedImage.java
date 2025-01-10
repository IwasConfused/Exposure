package io.github.mortuusars.exposure.client.image;

import net.minecraft.util.Mth;

public class ResizedImage implements Image {
    private final Image original;
    private final int width;
    private final int height;

    public ResizedImage(Image original, int width, int height) {
        this.original = original;
        this.width = width;
        this.height = height;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        double xRatio = (double) original.width() / width;
        double yRatio = (double) original.height() / height;

        int originalX = Mth.clamp(Mth.floor(x * xRatio), 0, original.width() - 1);
        int originalY = Mth.clamp(Mth.floor(y * yRatio), 0, original.height() - 1);

        return original.getPixelARGB(originalX, originalY);
    }

    @Override
    public void close() {
        original.close();
    }
}
