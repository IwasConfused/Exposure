package io.github.mortuusars.exposure.client.snapshot.saving;

import io.github.mortuusars.exposure.warehouse.PalettedImage;

public interface Saver {
    void save(PalettedImage imageData);
}
