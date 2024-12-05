package io.github.mortuusars.exposure.core.image.processing;

import io.github.mortuusars.exposure.core.image.CroppedImage;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.Rect2i;

@FunctionalInterface
public interface Crop {
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

    Image crop(Image image);

    default Crop then(Crop next) {
        return image -> next.crop(crop(image));
    }
}
