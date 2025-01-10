package io.github.mortuusars.exposure.client.image.renderable;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.image.processor.Processor;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.warehouse.ExposureData;

import java.util.function.Function;

public interface RenderableImage extends Image {
    RenderableImage EMPTY = new RenderableImage.Instance(Image.EMPTY, new RenderableImageIdentifier("-empty"));
    RenderableImage MISSING = new RenderableImage.Instance(Image.MISSING, new RenderableImageIdentifier("-missing"));

    Image getImage();
    RenderableImageIdentifier getIdentifier();

    default RenderableImage processWith(Function<Image, Image> transformFunction, String variant) {
        Image image = transformFunction.apply(getImage());
        RenderableImageIdentifier identifier = getIdentifier().appendVariant(variant);
        return new Instance(image, identifier);
    }

    default RenderableImage processWith(Processor processor) {
        return processWith(processor::process, processor.getIdentifier());
    }

//    static RenderableImage fromExposure(String id, ExposureData exposure) {
//        return new Instance(new PalettedImage(exposure.getWidth(), exposure.getHeight(), exposure.getPixels(),
//                Exposure.Registries.getColorPaletteOrDefault(Minecrft.level(), exposure.getPaletteId()).value()), new RenderableImageIdentifier(id));
//    }

    static RenderableImage of(String id, Image image) {
        return new Instance(image, new RenderableImageIdentifier(id));
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
