package io.github.mortuusars.exposure.client.image;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class CensoredImage implements Image {
    private final Image image;
    private final Integer[][] cache;

    public CensoredImage(Image image) {
        this.image = image;
        float blockSize = getBlockSize();
        this.cache = new Integer[Mth.ceil(width() / blockSize)][Mth.ceil(height() / blockSize)];
    }

    public int getBlockSize() {
        int largerSize = Math.max(width(), height());
        return Math.max(largerSize / 16, 1);
    }

    @Override
    public int width() {
        return image.width();
    }

    @Override
    public int height() {
        return image.height();
    }

    @Override
    public void close() {
        image.close();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        int blockSize = getBlockSize();
        int blockXIndex = x / blockSize;
        int blockYIndex = y / blockSize;
        int blockX = blockXIndex * blockSize;
        int blockY = blockYIndex * blockSize;

        Integer value = cache[blockXIndex][blockYIndex];

        if (value == null) {
            value = calculateAverageBlockColor(blockX, blockY, blockSize);
            cache[blockXIndex][blockYIndex] = value;
        }

        return value;
    }

    private int calculateAverageBlockColor(int startX, int startY, int blockSize) {
        int endX = Math.min(startX + blockSize, width());
        int endY = Math.min(startY + blockSize, height());

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

        return FastColor.ARGB32.color(avgA, avgR, avgG, avgB);
    }
}