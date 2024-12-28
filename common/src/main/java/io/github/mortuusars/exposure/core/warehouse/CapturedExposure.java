package io.github.mortuusars.exposure.core.warehouse;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CapturedExposure(int width,
                               int height,
                               byte[] pixels,
                               ColorPalette palette,
                               boolean isFromFile) {
    public static final StreamCodec<ByteBuf, CapturedExposure> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CapturedExposure::width,
            ByteBufCodecs.VAR_INT, CapturedExposure::height,
            ByteBufCodecs.byteArray(2048 * 2048), CapturedExposure::pixels,
            ColorPalette.STREAM_CODEC, CapturedExposure::palette,
            ByteBufCodecs.BOOL, CapturedExposure::isFromFile,
            CapturedExposure::new
    );

    public static final CapturedExposure EMPTY = new CapturedExposure(
            1, 1, new byte[]{0}, ColorPalette.MAP_COLORS, false);

    public CapturedExposure {
        Preconditions.checkArgument(width > 0, "Width should be larger than 0. %s", this);
        Preconditions.checkArgument(height > 0, "Height should be larger than 0. %s ", this);
        Preconditions.checkArgument(pixels.length == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixels.length, width, height, width * height);
    }
}
