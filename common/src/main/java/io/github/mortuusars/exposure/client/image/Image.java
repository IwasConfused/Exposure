package io.github.mortuusars.exposure.client.image;

public interface Image extends AutoCloseable {
    Image EMPTY = new EmptyImage();
    Image MISSING = new MissingImage();

    int getWidth();
    int getHeight();
    int getPixelARGB(int x, int y);
    default void close() {}

    default boolean isEmpty() {
        return getWidth() <= 1 && getHeight() <= 1 && getPixelARGB(0, 0) == 0x00000000;
    }

    default Image copy() {
        return PixelImage.copyFrom(this);
    }

    default RenderableImage toRenderable(ImageIdentifier identifier) {
        return new RenderableImage(this, identifier);
    }
}
