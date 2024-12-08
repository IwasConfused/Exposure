package io.github.mortuusars.exposure.client.image;

public abstract class WrappedImage implements Image {
    private final Image image;

    public WrappedImage(Image image) {
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
