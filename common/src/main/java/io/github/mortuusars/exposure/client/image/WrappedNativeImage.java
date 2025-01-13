package io.github.mortuusars.exposure.client.image;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.util.color.Color;

public class WrappedNativeImage implements Image {
    private final NativeImage nativeImage;

    public WrappedNativeImage(NativeImage nativeImage) {
        this.nativeImage = nativeImage;
    }

    @Override
    public int width() {
        return nativeImage.getWidth();
    }

    @Override
    public int height() {
        return nativeImage.getHeight();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return Color.ABGRtoARGB(nativeImage.getPixelRGBA(x, y));
    }

    @Override
    public void close() {
        nativeImage.close();
    }
}
