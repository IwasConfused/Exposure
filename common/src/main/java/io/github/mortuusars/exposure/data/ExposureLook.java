package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.client.image.processor.Processor;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExposureLook implements StringRepresentable {
    REGULAR("regular", Processor.EMPTY),
    AGED("aged", Processor.AGED),
    NEGATIVE("negative", Processor.NEGATIVE),
    NEGATIVE_FILM("negative_film", Processor.NEGATIVE_FILM);

    private final String name;
    private final Processor processor;

    ExposureLook(String name, Processor processor) {
        this.name = name;
        this.processor = processor;
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

    public Processor getModifier() {
        return processor;
    }

    public String getIdSuffix() {
        return this != REGULAR ? "_" + getSerializedName() : "";
    }
}