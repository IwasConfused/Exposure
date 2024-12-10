package io.github.mortuusars.exposure.client.snapshot.processing;

import io.github.mortuusars.exposure.client.image.CroppedImage;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ResizedImage;
import io.github.mortuusars.exposure.core.ChromaChannel;
import io.github.mortuusars.exposure.util.Rect2i;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Processor extends Function<Image, Image> {
    Processor EMPTY = image -> image;

    default Processor then(Processor next) {
        if (next.equals(EMPTY)) return this;
        return this.equals(EMPTY) ? next : image -> next.apply(apply(image));
    }

    default Processor thenIf(boolean condition, Processor next) {
        return condition ? then(next) : this;
    }

    default Processor thenIf(boolean condition, Supplier<Processor> next) {
        return condition ? then(next.get()) : this;
    }

    static Processor brightness(float stops) {
        return stops != 0 ? new BrightnessProcessor(stops) : EMPTY;
    }

    static Processor blackAndWhite() {
        return new BlackAndWhiteProcessor(1.15f);
    }

    static Processor blackAndWhite(float contrast) {
        return new BlackAndWhiteProcessor(contrast);
    }

    static Processor singleChannelBlackAndWhite(ChromaChannel chromaChannel) {
        return new SingleChannelBlackAndWhiteProcessor(chromaChannel);
    }

    interface Crop extends Processor {
        Crop SQUARE = image -> {
            int smallerSide = Math.min(image.getWidth(), image.getHeight());
            int x = (image.getWidth() - smallerSide) / 2;
            int y = (image.getHeight() - smallerSide) / 2;
            return new CroppedImage(image, new Rect2i(x, y, smallerSide, smallerSide));
        };

        static Crop factor(double factor) {
            return image -> {
                int newWidth = (int) (image.getWidth() * factor);
                int newHeight = (int) (image.getHeight() * factor);
                int x = (image.getWidth() - newWidth) / 2;
                int y = (image.getHeight() - newHeight) / 2;
                return new CroppedImage(image, new Rect2i(x, y, newWidth, newHeight));
            };
        }
    }

    interface Resize extends Processor {
        static Processor to(int width, int height) {
            return image -> new ResizedImage(image, width, height);
        }

        static Processor to(int size) {
            return to(size, size);
        }
    }
}
