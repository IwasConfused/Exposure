package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ModifiedImage;
import io.github.mortuusars.exposure.client.image.modifier.ImageModifier;

public interface PixelModifier extends ImageModifier {
    int modify(int colorARGB);

    default Image modify(Image image) {
        return new ModifiedImage(image, this::modify);
    }
}
