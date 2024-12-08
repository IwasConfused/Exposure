package io.github.mortuusars.exposure.core.image;

import io.github.mortuusars.exposure.client.image.Image;

public interface IdentifiableImage extends Image {
    String getId();
    default String getSuffix() {
        return "";
    }
}
