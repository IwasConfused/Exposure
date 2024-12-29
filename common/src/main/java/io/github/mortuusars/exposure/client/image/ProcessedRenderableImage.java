package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.client.image.pixel_modifiers.PixelModifier;

public class ProcessedRenderableImage extends ProcessedImage implements RenderableImage {
    private final RenderableImage image;
    private final PixelModifier pixelModifier;

    public ProcessedRenderableImage(RenderableImage image, PixelModifier modifier) {
        super(image, modifier::modifyPixel);
        this.image = image;
        pixelModifier = modifier;
    }

    @Override
    public String getIdentifier() {
        return image.getIdentifier() + "_" + pixelModifier.getIdentifier();
    }
}
