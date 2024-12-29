package io.github.mortuusars.exposure.client.image.renderable;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.image.ProcessedImage;
import io.github.mortuusars.exposure.client.image.pixel_modifiers.PixelModifier;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public interface RenderableImage extends Image {
    RenderableImage EMPTY = new RenderableImage.Instance(Image.EMPTY, new RenderableImageIdentifier("-empty"));
    RenderableImage MISSING = new RenderableImage.Instance(Image.MISSING, new RenderableImageIdentifier("-missing"));

    Image getImage();
    RenderableImageIdentifier getIdentifier();

    default RenderableImage modify(Function<Image, Image> transformFunction, String variant) {
        Image image = transformFunction.apply(getImage());
        RenderableImageIdentifier identifier = getIdentifier().appendVariant(variant);
        return new Instance(image, identifier);
    }

    default RenderableImage modify(PixelModifier pixelModifier) {
        return modify(image -> new ProcessedImage(image, pixelModifier::modifyPixel), pixelModifier.getIdentifier());
    }

    static RenderableImage fromExposure(PalettedExposure exposure, String id) {
        return new Instance(new PalettedImage(exposure), new RenderableImageIdentifier(id));
    }

    static RenderableImage fromResource(Image image, ResourceLocation location) {
        return new Instance(image, new RenderableImageIdentifier(location.toString()));
    }

    class Instance extends Image.Wrapped implements RenderableImage {
        private final RenderableImageIdentifier identifier;

        public Instance(Image image, RenderableImageIdentifier identifier) {
            super(image);
            this.identifier = identifier;
        }

        @Override
        public RenderableImageIdentifier getIdentifier() {
            return identifier;
        }
    }
}
