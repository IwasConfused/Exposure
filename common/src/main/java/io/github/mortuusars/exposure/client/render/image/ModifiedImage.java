package io.github.mortuusars.exposure.client.render.image;

import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.core.pixel_modifiers.PixelModifier;

public class ModifiedImage implements Image {
    protected final Image image;
    protected final PixelModifier modifier;
    protected String id;

    public ModifiedImage(Image image, PixelModifier modifier) {
        this.image = image;
        this.modifier = modifier;
        this.id = image.id() + modifier.getIdSuffix();
    }

    @Override
    public String id() {
        return id;
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
    public int getPixelABGR(int x, int y) {
        return modifier.modifyPixel(image.getPixelABGR(x, y));
    }

    @Override
    public void close() {
        image.close();
    }
}
