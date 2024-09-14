package io.github.mortuusars.exposure.warehouse;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ExposureClientData(int width,
                                 int height,
                                 byte[] pixels,
                                 boolean fromFile,
                                 CompoundTag extraData) {
    public static final Codec<byte[]> BYTE_ARRAY_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(byte[] input, DynamicOps<T> ops, T prefix) {
            ListBuilder<T> builder = ops.listBuilder();
            for (byte inp : input)
                builder.add(ops.createByte(inp));
            return builder.build(prefix);
        }

        @Override
        public <T> DataResult<Pair<byte[], T>> decode(DynamicOps<T> ops, T input) {
            return ops.getByteBuffer(input).map(t -> Pair.of(t.array(), input));
        }
    };

    public static final Codec<ExposureClientData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("width").forGetter(ExposureClientData::width),
            Codec.INT.fieldOf("height").forGetter(ExposureClientData::height),
            BYTE_ARRAY_CODEC.fieldOf("pixels").forGetter(ExposureClientData::pixels),
            Codec.BOOL.optionalFieldOf("from_file", false).forGetter(ExposureClientData::fromFile),
            CompoundTag.CODEC.optionalFieldOf("extra_data", new CompoundTag()).forGetter(ExposureClientData::extraData)
    ).apply(instance, ExposureClientData::new));

    public static final StreamCodec<ByteBuf, ExposureClientData> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull ExposureClientData decode(ByteBuf buffer) {
            return new ExposureClientData(
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.byteArray(2048 * 2048).decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.COMPOUND_TAG.decode(buffer));
        }

        public void encode(ByteBuf buffer, ExposureClientData data) {
            ByteBufCodecs.VAR_INT.encode(buffer, data.width());
            ByteBufCodecs.VAR_INT.encode(buffer, data.width());
            ByteBufCodecs.byteArray(2048 * 2048).encode(buffer, data.pixels());
            ByteBufCodecs.BOOL.encode(buffer, data.fromFile());
            ByteBufCodecs.COMPOUND_TAG.encode(buffer, data.extraData());
        }
    };

    public static final ExposureClientData EMPTY = new ExposureClientData(
            1, 1, new byte[]{0}, false, new CompoundTag());

    public ExposureClientData {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(height >= 0, "Height cannot be negative.");
        if (pixels.length > width * height)
            Exposure.LOGGER.warn("Pixel count '{}' is larger than image dimensions of '{}x{}' could fit.", pixels.length, width, height);
    }
}
