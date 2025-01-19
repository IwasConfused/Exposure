package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.client.image.modifier.Modifier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExposureLook implements StringRepresentable {
    REGULAR("regular", Modifier.EMPTY),
    AGED("aged", Modifier.AGED),
    NEGATIVE("negative", Modifier.NEGATIVE),
    NEGATIVE_FILM("negative_film", Modifier.NEGATIVE_FILM);

    private final String name;
    private final Modifier modifier;

    ExposureLook(String name, Modifier modifier) {
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

    public Modifier getFilter() {
        return modifier;
    }

    public String getIdSuffix() {
        return this != REGULAR ? "_" + getSerializedName() : "";
    }
}