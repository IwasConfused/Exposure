package io.github.mortuusars.exposure.util;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum ChromaticChannel implements StringRepresentable {
    RED(0xFFD8523E),
    GREEN(0xFF7BC64B),
    BLUE(0xFF4E73CE);

    // Used in UI to color text, etc.
    private final int color;

    ChromaticChannel(int color) {
        this.color = color;
    }

    public int getRepresentationColor() {
        return color;
    }

    public static Optional<ChromaticChannel> fromStack(ItemStack stack) {
        if (stack.is(Exposure.Tags.Items.RED_FILTERS))
            return Optional.of(RED);
        else if (stack.is(Exposure.Tags.Items.GREEN_FILTERS))
            return Optional.of(GREEN);
        else if (stack.is(Exposure.Tags.Items.BLUE_FILTERS))
            return Optional.of(BLUE);
        else
            return Optional.empty();
    }

    public static ChromaticChannel fromStringOrDefault(String serializedName, ChromaticChannel defaultValue) {
        for (ChromaticChannel value : values()) {
            if (value.getSerializedName().equals(serializedName))
                return value;
        }
        return defaultValue;
    }

    public static Optional<ChromaticChannel> fromString(String serializedName) {
        for (ChromaticChannel value : values()) {
            if (value.getSerializedName().equals(serializedName))
                return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull String getSerializedName() {
        return toString().toLowerCase();
    }
}
