package io.github.mortuusars.exposure.client.image;

import net.minecraft.util.FastColor;

public class CensoredImage implements Image {
    private final Image image;

    public CensoredImage(Image image) {
        this.image = image;
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public void close() {
        image.close();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        // Apply obscuring effect (e.g., pixelation)
        int blockSize = 8; // Size of the pixelation block (tune for effect)
        int blockX = (x / blockSize) * blockSize;
        int blockY = (y / blockSize) * blockSize;

        return calculateAverageBlockColor(blockX, blockY, blockSize);
    }

    private int calculateAverageBlockColor(int startX, int startY, int blockSize) {
        int endX = Math.min(startX + blockSize, getWidth());
        int endY = Math.min(startY + blockSize, getHeight());

        int totalR = 0, totalG = 0, totalB = 0, totalA = 0;
        int pixelCount = 0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int pixel = image.getPixelARGB(x, y);
                totalA += (pixel >> 24) & 0xFF;
                totalR += (pixel >> 16) & 0xFF;
                totalG += (pixel >> 8) & 0xFF;
                totalB += pixel & 0xFF;
                pixelCount++;
            }
        }

        if (pixelCount == 0) return 0; // Avoid division by zero

        int avgA = totalA / pixelCount;
        int avgR = totalR / pixelCount;
        int avgG = totalG / pixelCount;
        int avgB = totalB / pixelCount;

        return FastColor.ARGB32.color(avgA << 24, avgR << 16, avgG << 8, avgB);
    }
}