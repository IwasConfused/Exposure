package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;

public interface Image extends AutoCloseable {
    RenderableImage EMPTY = new EmptyImage();
    RenderableImage MISSING = new MissingImage();

    int getWidth();
    int getHeight();
    int getPixelARGB(int x, int y);
    default void close() {}

    static void validate(int width, int height, int pixelCount) {
        Preconditions.checkArgument(width > 0, "Width should be larger than 0. %s", width);
        Preconditions.checkArgument(height > 0, "Height should be larger than 0. %s ", height);
        Preconditions.checkArgument(pixelCount == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixelCount, width, height, width * height);
    }

//    default boolean isEmpty() {
//        return this.equals(EMPTY) || (getWidth() <= 1 && getHeight() <= 1 && getPixelARGB(0, 0) == 0x00000000);
//    }

//    default Image copy() {
//        return PixelImage.copyFrom(this);
//    }
//
//    default RenderableImage toRenderable(ImageIdentifier identifier) {
//        return new RenderableImage(this, identifier);
//    }
}
