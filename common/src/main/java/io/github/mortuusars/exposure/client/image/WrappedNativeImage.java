package io.github.mortuusars.exposure.client.image;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.core.image.color.Color;

public class WrappedNativeImage implements Image {
    private final NativeImage nativeImage;

    public WrappedNativeImage(NativeImage nativeImage) {
        this.nativeImage = nativeImage;
    }

    @Override
    public int getWidth() {
        return nativeImage.getWidth();
    }

    @Override
    public int getHeight() {
        return nativeImage.getHeight();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return Color.BGRtoRGB(nativeImage.getPixelRGBA(x, y));
    }

    @Override
    public void close() {
        nativeImage.close();
    }
}
