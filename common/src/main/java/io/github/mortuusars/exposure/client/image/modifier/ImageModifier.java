package io.github.mortuusars.exposure.client.image.modifier;

import io.github.mortuusars.exposure.client.image.CensoredImage;
import io.github.mortuusars.exposure.client.image.CroppedImage;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ResizedImage;
import io.github.mortuusars.exposure.client.image.modifier.pixel.*;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.util.Rect2i;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface ImageModifier {
    /**
     * Unique identifier to differentiate between rendered images.<br>
     * It is used in {@link net.minecraft.resources.ResourceLocation} so choose valid chars as invalid ones will be removed.<br>
     * To make it more readable, separate parts by dash: brightness-1.5
     * and join multiple identifiers with underscore: brightness-1.5_contrast-0.8
     */
    String getIdentifier();
    Image modify(Image image);

    // --

    ImageModifier EMPTY = new Instance("", Function.identity());

    ImageModifier CENSORED = new Instance("censored", CensoredImage::new);
    ImageModifier NEGATIVE = new NegativeModifier();
    ImageModifier NEGATIVE_FILM = new NegativeFilmModifier();
    ImageModifier AGED = new AgedHSBModifier(0xD9A863, 0.65f, 40, 255);
    ImageModifier BLACK_AND_WHITE = new BlackAndWhiteModifier(0.299f, 0.587f, 0.114f);

    static ImageModifier brightness(ShutterSpeed shutterSpeed) {
        return shutterSpeed != ShutterSpeed.DEFAULT ? new BrightnessModifier(shutterSpeed) : EMPTY;
    }

    static ImageModifier singleChannelBlackAndWhite(ColorChannel colorChannel) {
        return switch (colorChannel) {
            case RED -> new BlackAndWhiteModifier(1f, 0.05f, 0.05f);
            case GREEN -> new BlackAndWhiteModifier(0.05f, 1f, 0.05f);
            case BLUE -> new BlackAndWhiteModifier(0.05f, 0.05f, 1f);
        };
    }

    static ImageModifier contrast(float value) {
        return new ContrastModifier(value);
    }

    interface Crop extends ImageModifier {
        ImageModifier SQUARE_CENTER = new Instance("crop-square", image -> {
            int smallerSide = Math.min(image.width(), image.height());
            int x = (image.width() - smallerSide) / 2;
            int y = (image.height() - smallerSide) / 2;
            return new CroppedImage(image, new Rect2i(x, y, smallerSide, smallerSide));
        });

        static ImageModifier factor(double factor) {
            if (factor == 1f) return EMPTY;
            double clampedFactor = Math.min(1.0, factor);
            return new Instance("crop-factor-" + String.format("%,.4f", clampedFactor), image -> {
                int newWidth = (int) (image.width() * clampedFactor);
                int newHeight = (int) (image.height() * clampedFactor);
                int x = (image.width() - newWidth) / 2;
                int y = (image.height() - newHeight) / 2;
                return new CroppedImage(image, new Rect2i(x, y, newWidth, newHeight));
            });
        }
    }

    interface Resize extends ImageModifier {
        static ImageModifier to(int width, int height) {
            return new Instance("resized-%sx%s".formatted(width, height), image -> new ResizedImage(image, width, height));
        }

        static ImageModifier to(int size) {
            return to(size, size);
        }
    }

    // --

    static ImageModifier composite(ImageModifier... modifiers) {
        if (modifiers.length == 0) return EMPTY;
        return new Composite(modifiers);
    }

    static Function<Image, Image> chain(ImageModifier... modifiers) {
        return composite(modifiers)::modify;
    }

    static ImageModifier optional(boolean condition, ImageModifier modifier) {
        return condition ? modifier : EMPTY;
    }

    static ImageModifier optional(boolean condition, Supplier<ImageModifier> supplier) {
        return condition ? supplier.get() : EMPTY;
    }

    static <T> ImageModifier optional(Optional<T> optional, Function<T, ImageModifier> ifPresent) {
        return optional.map(ifPresent).orElse(ImageModifier.EMPTY);
    }

    record Instance(String identifier, Function<Image, Image> processingFunction) implements ImageModifier {
        @Override
        public Image modify(Image image) {
            return processingFunction.apply(image);
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }
    }

    record Composite(ImageModifier... modifiers) implements ImageModifier {
        @Override
        public Image modify(Image image) {
            for (ImageModifier modifier : modifiers) {
                image = modifier.modify(image);
            }
            return image;
        }

        @Override
        public String getIdentifier() {
            return Arrays.stream(modifiers)
                    .filter(filter -> !filter.equals(EMPTY))
                    .map(ImageModifier::getIdentifier)
                    .collect(Collectors.joining("_"));
        }
    }
}
