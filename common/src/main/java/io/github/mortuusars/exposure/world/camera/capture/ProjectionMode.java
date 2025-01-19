package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;


//TODO: remove? exported exposures should be loaded properly even with dithering (with no changes)
public enum ProjectionMode implements StringRepresentable {
    DITHERED("dithered"),
    CLEAN("clean");

    private static final IntFunction<ProjectionMode> BY_ID =
            ByIdMap.continuous(ProjectionMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<ProjectionMode> CODEC = StringRepresentable.fromEnum(ProjectionMode::values);
    public static final StreamCodec<ByteBuf, ProjectionMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ProjectionMode::ordinal);

    private final String name;

    ProjectionMode(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public Component translate() {
        return Component.translatable("item.exposure.interplanar_projector.mode." + getSerializedName());
    }

    public ProjectionMode cycle() {
        return BY_ID.apply(ordinal() + 1);
    }
}
