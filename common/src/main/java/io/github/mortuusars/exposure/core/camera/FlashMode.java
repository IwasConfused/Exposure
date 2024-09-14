package io.github.mortuusars.exposure.core.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum FlashMode implements StringRepresentable {
    OFF("off"),
    ON("on"),
    AUTO("auto");

    public static final Codec<FlashMode> CODEC = StringRepresentable.fromEnum(FlashMode::values);
    public static final StreamCodec<ByteBuf, FlashMode> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(FlashMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), FlashMode::ordinal);

    private final String id;

    FlashMode(String id) {
        this.id = id;
    }

    public static FlashMode byIdOrOff(String id) {
        for (FlashMode guide : values()) {
            if (guide.id.equals(id))
                return guide;
        }

        return OFF;
    }

    public String getId() {
        return id;
    }

    @Override
    public @NotNull String getSerializedName() {
        return id;
    }

    public MutableComponent translate() {
        return Component.translatable("gui." + Exposure.ID + ".flash_mode." + id);
    }
}
