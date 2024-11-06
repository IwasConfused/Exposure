package io.github.mortuusars.exposure.core.image;

public interface Image {
    Image EMPTY = new EmptyImage();
    Image MISSING = new MissingImage();

    String id();
    int getWidth();
    int getHeight();
    int getPixelABGR(int x, int y);
    default void close() {}
}
