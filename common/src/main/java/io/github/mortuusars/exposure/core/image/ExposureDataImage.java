package io.github.mortuusars.exposure.core.image;

import io.github.mortuusars.exposure.warehouse.ExposureData;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public record ExposureDataImage(String id, ExposureData exposureData) implements Image {
    public ExposureDataImage(String id, @NotNull ExposureData exposureData) {
        this.id = id;
        this.exposureData = exposureData;
    }

    public int getWidth() {
        return exposureData.getWidth();
    }

    public int getHeight() {
        return exposureData.getHeight();
    }

    public int getPixelABGR(int x, int y) {
        //TODO: custom palette
        return MapColor.getColorFromPackedId(exposureData.getPixel(x, y));
    }
}
