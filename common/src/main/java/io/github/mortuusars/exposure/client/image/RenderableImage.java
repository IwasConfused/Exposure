package io.github.mortuusars.exposure.client.image;

import io.github.mortuusars.exposure.client.image.pixel_modifiers.PixelModifier;

import java.util.function.Function;

public class RenderableImage extends WrappedImage {
    private final ImageIdentifier identifier;

    public RenderableImage(Image image, ImageIdentifier identifier) {
        super(image);
        this.identifier = identifier;
    }

    public ImageIdentifier getIdentifier() {
        return identifier;
    }

    public RenderableImage processWith(PixelModifier pixelProcessor) {
        ProcessedImage image = new ProcessedImage(getImage(), pixelProcessor::modifyPixel);
        return new RenderableImage(image, identifier.appendVariants(pixelProcessor.getIdentifier()));
    }

    public RenderableImage wrapIn(Function<Image, Image> wrappingFunc, String... variantNames) {
        return new RenderableImage(wrappingFunc.apply(getImage()), identifier.appendVariants(variantNames));
    }
}
