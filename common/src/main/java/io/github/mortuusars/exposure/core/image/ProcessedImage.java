package io.github.mortuusars.exposure.core.image;

import java.util.function.Function;

public class ProcessedImage implements Image {
    private final Image original;
    private final Function<Integer, Integer> pixelProcessing;

    public ProcessedImage(Image original, Function<Integer, Integer> pixelProcessing) {
        this.original = original;
        this.pixelProcessing = pixelProcessing;
    }

    @Override
    public int getWidth() {
        return original.getWidth();
    }

    @Override
    public int getHeight() {
        return original.getHeight();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return pixelProcessing.apply(original.getPixelARGB(x, y));
    }

    @Override
    public void close() {
        original.close();
    }
}
