package io.github.mortuusars.exposure.client.image.modifier;

import io.github.mortuusars.exposure.client.image.CensoredImage;
import io.github.mortuusars.exposure.client.image.CroppedImage;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ResizedImage;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.util.Rect2i;
import net.minecraft.util.StringUtil;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface Modifier {
    Image modify(Image image);
    String getIdentifier();

    // --

    Modifier EMPTY = new Instance("", Function.identity());

    Modifier CENSORED = new Instance("censored", CensoredImage::new);
    Modifier NEGATIVE = new NegativeModifier();
    Modifier NEGATIVE_FILM = new NegativeFilmModifier();
    Modifier AGED = new AgedHSBModifier(0xD9A863, 0.65f, 40, 255);
    Modifier BLACK_AND_WHITE = new BlackAndWhiteModifier(0.299F, 0.587F, 0.114F);

    static Modifier brightness(float stops) {
        return stops != 0 ? new BrightnessModifier(stops) : EMPTY;
    }

    static Modifier singleChannelBlackAndWhite(ColorChannel colorChannel) {
        return switch (colorChannel) {
            case RED -> new BlackAndWhiteModifier(1F, 0.05F, 0.05F);
            case GREEN -> new BlackAndWhiteModifier(0.05F, 1F, 0.05F);
            case BLUE -> new BlackAndWhiteModifier(0.05F, 0.05F, 1F);
        };
    }

    static Modifier contrast(float value) {
        return new ContrastModifier(value);
    }

    interface Crop extends Modifier {
        Modifier SQUARE_CENTER = new Instance("crop-square", image -> {
            int smallerSide = Math.min(image.width(), image.height());
            int x = (image.width() - smallerSide) / 2;
            int y = (image.height() - smallerSide) / 2;
            return new CroppedImage(image, new Rect2i(x, y, smallerSide, smallerSide));
        });

        static Modifier factor(double factor) {
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

    interface Resize extends Modifier {
        static Modifier to(int width, int height) {
            return new Instance("resized-%sx%s".formatted(width, height), image -> new ResizedImage(image, width, height));
        }

        static Modifier to(int size) {
            return to(size, size);
        }
    }

    // --

    static Modifier composite(Modifier... modifiers) {
        if (modifiers.length == 0) return EMPTY;
        return new Composite(modifiers);
    }

    static Function<Image, Image> chain(Modifier... modifiers) {
        return composite(modifiers)::modify;
    }

    static Modifier optional(boolean condition, Supplier<Modifier> supplier) {
        return condition ? supplier.get() : EMPTY;
    }

    static <T> Modifier optional(Optional<T> optional, Function<T, Modifier> ifPresent) {
        return optional.map(ifPresent).orElse(Modifier.EMPTY);
    }

//    default Modifier then(Modifier next) {
//        if (next.equals(EMPTY)) return this;
//        if (this.equals(EMPTY)) return next;
//        Modifier current = this;
//        return new Modifier() {
//            @Override
//            public Image modify(Image image) {
//                return next.modify(current.modify(image));
//            }
//
//            @Override
//            public String getIdentifier() {
//                return combineIdentifiers(this, next);
//            }
//        };
//    }
//
//    static String combineIdentifiers(Modifier first, Modifier second) {
//        String firstId = first.getIdentifier();
//        String secondId = second.getIdentifier();
//        if (StringUtil.isBlank(firstId)) return secondId;
//        if (StringUtil.isBlank(secondId)) return firstId;
//        return firstId + '_' + secondId;
//    }

    record Instance(String identifier, Function<Image, Image> processingFunction) implements Modifier {
        @Override
        public Image modify(Image image) {
            return processingFunction.apply(image);
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }
    }

    record Composite(Modifier... modifiers) implements Modifier {
        @Override
        public Image modify(Image image) {
            for (Modifier modifier : modifiers) {
                if (modifier.equals(EMPTY)) continue;
                image = modifier.modify(image);
            }
            return image;
        }

        @Override
        public String getIdentifier() {
            return Arrays.stream(modifiers)
                    .filter(filter -> !filter.equals(EMPTY))
                    .map(Modifier::getIdentifier)
                    .collect(Collectors.joining("_"));
        }
    }
}
