package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.client.image.pixel_modifiers.PixelModifier;

public interface RenderableImage extends Image {
    String getIdentifier();

    default RenderableImage processWith(PixelModifier modifier) {
        return new ProcessedRenderableImage(this, modifier);
    }
}
