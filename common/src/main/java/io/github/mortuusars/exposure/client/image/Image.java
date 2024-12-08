package io.github.mortuusars.exposure.client.image;

public interface Image extends AutoCloseable {
    Image EMPTY = new EmptyImage();
    Image MISSING = new MissingImage();

    int getWidth();
    int getHeight();
    int getPixelARGB(int x, int y);
    default void close() {}

    default Image copy() {
        return PixelImage.copyFrom(this);
    }

    default RenderableImage toRenderable(ImageIdentifier identifier) {
        return new RenderableImage(this, identifier);
    }
}
