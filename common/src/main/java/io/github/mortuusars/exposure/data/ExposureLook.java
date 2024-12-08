package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.client.image.pixel_modifiers.PixelModifier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExposureLook implements StringRepresentable {
    REGULAR("regular", PixelModifier.EMPTY),
    AGED("aged", PixelModifier.AGED),
    NEGATIVE("negative", PixelModifier.NEGATIVE),
    NEGATIVE_FILM("negative_film", PixelModifier.NEGATIVE_FILM);

    private final String name;
    private final PixelModifier modifier;

    ExposureLook(String name, PixelModifier modifier) {
        this.name = name;
        this.modifier = modifier;
    }

    public static @Nullable ExposureLook byName(String name) {
        for (ExposureLook value : values()) {
            if (value.getSerializedName().equals(name))
                return value;
        }

        return null;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public PixelModifier getModifier() {
        return modifier;
    }

    public String getIdSuffix() {
        return this != REGULAR ? "_" + getSerializedName() : "";
    }
}