package io.github.mortuusars.exposure.foundation.warehouse;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ExposureClientData(int width,
                                 int height,
                                 byte[] pixels,
                                 ColorPalette palette,
                                 boolean fromFile,
                                 CompoundTag extraData) {
    public static final StreamCodec<ByteBuf, ExposureClientData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ExposureClientData::width,
            ByteBufCodecs.VAR_INT, ExposureClientData::height,
            ByteBufCodecs.byteArray(2048 * 2048), ExposureClientData::pixels,
            ColorPalette.STREAM_CODEC, ExposureClientData::palette,
            ByteBufCodecs.BOOL, ExposureClientData::fromFile,
            ByteBufCodecs.COMPOUND_TAG, ExposureClientData::extraData,
            ExposureClientData::new
    );

    public static final ExposureClientData EMPTY = new ExposureClientData(
            1, 1, new byte[]{0}, ColorPalette.MAP_COLORS, false, new CompoundTag());

    public ExposureClientData {
        Preconditions.checkArgument(width >= 0, "Width should be larger than 0. %s", this);
        Preconditions.checkArgument(height >= 0, "Height should be larger than 0. %s ", this);
        Preconditions.checkArgument(pixels.length == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixels.length, width, height, width * height);
    }
}
