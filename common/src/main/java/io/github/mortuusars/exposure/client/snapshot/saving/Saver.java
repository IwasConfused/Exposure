package io.github.mortuusars.exposure.client.snapshot.saving;

import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.warehouse.PalettedImage;

public interface Saver {
    void save(Image imageData);
}
