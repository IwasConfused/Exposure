package io.github.mortuusars.exposure.core.image;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ExposureDataImage(String id, ExposureData exposureData) implements Image {
    public ExposureDataImage(String id, @NotNull ExposureData exposureData) {
        this.id = filterId(id);
        this.exposureData = exposureData;
    }

    private static String filterId(String imageId) {
        String id = Exposure.ID + "/" + imageId.toLowerCase();
        id = id.replace(':', '_');

        // Player nicknames can have non az09 chars
        // we need to remove all invalid chars from the imageId to create ResourceLocation,
        // otherwise it crashes
        Pattern pattern = Pattern.compile("[^a-z0-9_.-]");
        Matcher matcher = pattern.matcher(id);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, String.valueOf(matcher.group().hashCode()));
        }
        matcher.appendTail(sb);

        return sb.toString();
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
