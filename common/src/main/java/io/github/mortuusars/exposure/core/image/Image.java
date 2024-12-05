package io.github.mortuusars.exposure.core.image;

public interface Image extends AutoCloseable {
    IdentifiableImage EMPTY = new EmptyImage();
    IdentifiableImage MISSING = new MissingImage();

    int getWidth();
    int getHeight();
    int getPixelARGB(int x, int y);
    default void close() {}

    default Image copy() {
        return PixelImage.copyFrom(this);
    }
}
