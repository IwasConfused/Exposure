package io.github.mortuusars.exposure.core.image;

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
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        double xRatio = (double) original.getWidth() / width;
        double yRatio = (double) original.getHeight() / height;

        int originalX = Mth.clamp(Mth.floor(x * xRatio), 0, original.getWidth() - 1);
        int originalY = Mth.clamp(Mth.floor(y * yRatio), 0, original.getHeight() - 1);

        return original.getPixelARGB(originalX, originalY);
    }

    @Override
    public void close() {
        original.close();
    }
}
