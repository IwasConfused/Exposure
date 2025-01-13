package io.github.mortuusars.exposure.client.image.processor;

import io.github.mortuusars.exposure.client.image.CensoredImage;
import io.github.mortuusars.exposure.client.image.CroppedImage;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ResizedImage;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.util.Rect2i;
import net.minecraft.util.StringUtil;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Processor {
    Image process(Image image);

    String getIdentifier();

    // --

    Processor EMPTY = new Processor() {
        @Override
        public Image process(Image image) {
            return image;
        }

        @Override
        public String getIdentifier() {
            return "";
        }
    };

    Processor CENSORED = new Instance("censored", CensoredImage::new);

    Processor NEGATIVE = new NegativeProcessor();
    Processor NEGATIVE_FILM = new NegativeFilmProcessor();
    Processor AGED = new AgedHSBProcessor(0xD9A863, 0.65f, 40, 255);

    static Processor brightness(float stops) {
        return stops != 0 ? new BrightnessProcessor(stops) : EMPTY;
    }

    static Processor blackAndWhite(float contrast) {
        return new BlackAndWhiteProcessor(contrast);
    }

    static Processor singleChannelBlackAndWhite(ColorChannel colorChannel) {
        return new SingleChannelBlackAndWhiteProcessor(colorChannel);
    }

    interface Crop extends Processor {
        Processor SQUARE_CENTER = new Instance("crop-square", image -> {
            int smallerSide = Math.min(image.width(), image.height());
            int x = (image.width() - smallerSide) / 2;
            int y = (image.height() - smallerSide) / 2;
            return new CroppedImage(image, new Rect2i(x, y, smallerSide, smallerSide));
        });

        static Processor factor(double factor) {
            return new Instance("crop-factor-" + String.format("%,.4f", factor), image -> {
                int newWidth = (int) (image.width() * factor);
                int newHeight = (int) (image.height() * factor);
                int x = (image.width() - newWidth) / 2;
                int y = (image.height() - newHeight) / 2;
                return new CroppedImage(image, new Rect2i(x, y, newWidth, newHeight));
            });
        }
    }

    interface Resize extends Processor {
        static Processor to(int width, int height) {
            return new Instance("resized-%sx%s".formatted(width, height), image -> new ResizedImage(image, width, height));
        }

        static Processor to(int size) {
            return to(size, size);
        }
    }

    // --

    default Processor then(Processor next) {
        if (next.equals(EMPTY)) return this;
        if (this.equals(EMPTY)) return next;
        Processor current = this;
        return new Processor() {
            @Override
            public Image process(Image image) {
                return next.process(current.process(image));
            }

            @Override
            public String getIdentifier() {
                return combineIdentifiers(this, next);
            }
        };
    }

    default Processor thenIf(boolean condition, Processor next) {
        return condition ? then(next) : this;
    }

    default Processor thenIf(boolean condition, Supplier<Processor> next) {
        return condition ? then(next.get()) : this;
    }

    static String combineIdentifiers(Processor first, Processor second) {
        String firstId = first.getIdentifier();
        String secondId = second.getIdentifier();
        if (StringUtil.isBlank(firstId)) return secondId;
        if (StringUtil.isBlank(secondId)) return firstId;
        return firstId + '_' + secondId;
    }

    record Instance(String identifier, Function<Image, Image> processingFunction) implements Processor {
        @Override
        public Image process(Image image) {
            return processingFunction.apply(image);
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }
    }
}
