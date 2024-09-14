package io.github.mortuusars.exposure.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public enum InterplanarProjectorMode implements StringRepresentable {
    DITHERED("dithered"),
    CLEAN("clean");

    private static final IntFunction<InterplanarProjectorMode> BY_ID =
            ByIdMap.continuous(InterplanarProjectorMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<InterplanarProjectorMode> CODEC = StringRepresentable.fromEnum(InterplanarProjectorMode::values);
    public static final StreamCodec<ByteBuf, InterplanarProjectorMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, InterplanarProjectorMode::ordinal);

    private final String name;

    InterplanarProjectorMode(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public Component translate() {
        return Component.translatable("item.exposure.interplanar_projector.mode." + getSerializedName());
    }

    public InterplanarProjectorMode cycle() {
        return BY_ID.apply(ordinal() + 1);
    }
}
