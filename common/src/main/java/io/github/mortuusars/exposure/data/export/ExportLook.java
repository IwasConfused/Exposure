package io.github.mortuusars.exposure.data.export;

import io.github.mortuusars.exposure.client.image.modifier.ImageModifier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExportLook implements StringRepresentable {
    REGULAR("regular", ImageModifier.EMPTY),
    AGED("aged", ImageModifier.AGED),
    NEGATIVE("negative", ImageModifier.NEGATIVE),
    NEGATIVE_FILM("negative_film", ImageModifier.NEGATIVE_FILM);

    private final String name;
    private final ImageModifier modifier;

    ExportLook(String name, ImageModifier modifier) {
        this.name = name;
        this.modifier = modifier;
    }

    public static @Nullable ExportLook byName(String name) {
        for (ExportLook value : values()) {
            if (value.getSerializedName().equals(name))
                return value;
        }

        return null;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public ImageModifier getModifier() {
        return modifier;
    }

    public String getIdSuffix() {
        return this != REGULAR ? "_" + getSerializedName() : "";
    }
}