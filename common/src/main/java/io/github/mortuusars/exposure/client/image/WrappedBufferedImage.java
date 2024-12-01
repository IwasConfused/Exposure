package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.core.image.Image;

import java.awt.image.BufferedImage;

public class WrappedBufferedImage implements Image {
    private final BufferedImage bufferedImage;

    public WrappedBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    @Override
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return bufferedImage.getHeight();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return bufferedImage.getRGB(x, y);
    }
}
