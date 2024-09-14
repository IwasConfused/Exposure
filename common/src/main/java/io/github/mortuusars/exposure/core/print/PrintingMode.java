package io.github.mortuusars.exposure.core.print;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PrintingMode implements StringRepresentable {
    REGULAR("regular"),
    CHROMATIC("chromatic");

    private final String name;

    PrintingMode(String name) {
        this.name = name;
    }

    public static PrintingMode fromStringOrDefault(String serializedName, PrintingMode defaultValue) {
        for (PrintingMode value : values()) {
            if (value.getSerializedName().equals(serializedName))
                return value;
        }
        return defaultValue;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
