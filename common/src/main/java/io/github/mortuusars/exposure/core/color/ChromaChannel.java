package io.github.mortuusars.exposure.core.color;

import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum ChromaChannel implements StringRepresentable {
    RED(0xFFD8523E),
    GREEN(0xFF7BC64B),
    BLUE(0xFF4E73CE);

    public static final StreamCodec<ByteBuf, ChromaChannel> STREAM_CODEC =
            ByteBufCodecs.idMapper(id -> ChromaChannel.values()[id], ChromaChannel::ordinal);

    // Used in UI to color text, etc.
    private final int color;

    ChromaChannel(int color) {
        this.color = color;
    }

    public int getRepresentationColor() {
        return color;
    }

    public static Optional<ChromaChannel> fromFilterStack(ItemStack stack) {
        if (stack.is(Exposure.Tags.Items.RED_FILTERS))
            return Optional.of(RED);
        else if (stack.is(Exposure.Tags.Items.GREEN_FILTERS))
            return Optional.of(GREEN);
        else if (stack.is(Exposure.Tags.Items.BLUE_FILTERS))
            return Optional.of(BLUE);
        else
            return Optional.empty();
    }

    public static ChromaChannel fromStringOrDefault(String serializedName, ChromaChannel defaultValue) {
        for (ChromaChannel value : values()) {
            if (value.getSerializedName().equals(serializedName))
                return value;
        }
        return defaultValue;
    }

    public static Optional<ChromaChannel> fromString(String serializedName) {
        for (ChromaChannel value : values()) {
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
