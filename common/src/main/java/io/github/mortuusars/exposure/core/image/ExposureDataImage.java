package io.github.mortuusars.exposure.core.image;

import io.github.mortuusars.exposure.warehouse.ExposureData;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class ExposureDataImage implements IImage {
    private final String name;
    private final ExposureData exposureData;

    public ExposureDataImage(String name, @NotNull ExposureData exposureData) {
        this.name = name;
        this.exposureData = exposureData;
    }

    @Override
    public String getImageId() {
        return name;
    }

    public ExposureData getExposureData() {
        return exposureData;
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
