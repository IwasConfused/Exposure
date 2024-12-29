package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;

public interface Image extends AutoCloseable {
    Image EMPTY = new EmptyImage();
    Image MISSING = new MissingImage();

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

    abstract class Wrapped implements Image {
        private final Image image;

        public Wrapped(Image image) {
            this.image = image;
        }

        public Image getImage() {
            return image;
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
        public int getPixelARGB(int x, int y) {
            return image.getPixelARGB(x, y);
        }

        @Override
        public void close() {
            image.close();
        }
    }
}
