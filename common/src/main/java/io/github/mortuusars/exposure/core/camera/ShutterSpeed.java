package io.github.mortuusars.exposure.core.camera;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.SharedConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class ShutterSpeed {
    public static final Codec<ShutterSpeed> CODEC = Codec.STRING.xmap(ShutterSpeed::new, ShutterSpeed::getNotation);

    public static final StreamCodec<ByteBuf, ShutterSpeed> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ShutterSpeed::getNotation,
            ShutterSpeed::new
    );

    public static final ShutterSpeed DEFAULT = new ShutterSpeed("1/60");

    private final float valueMilliseconds;
    private final String notation;

    /**
     * Expected format is 1/60, 1/125, 2", 15", etc.
     */
    public ShutterSpeed(String notation) {
        notation = notation.trim();

        if (notation.endsWith("\"")) {
            this.valueMilliseconds = Integer.parseInt(notation.replace("\"", "")) * 1000;
            this.notation = notation;
        }
        else if (notation.contains("1/")) {
            this.valueMilliseconds = 1f / Integer.parseInt(notation.replace("1/", "")) * 1000;
            this.notation = notation;
        }
        else {
            throw new IllegalArgumentException("'{}' is not a valid shutter speed. Format should be 1/60, 2\", etc.");
        }
    }

    public String getNotation() {
        return notation;
    }

    public float getDurationMilliseconds() {
        return valueMilliseconds;
    }

    /**
     * Should be at least 1 tick. Otherwise, it's probably not going to work correctly.
     */
    public int getDurationTicks() {
        return Math.max(1, (int)(valueMilliseconds / SharedConstants.MILLIS_PER_TICK));
    }

    public boolean shouldCauseTickingSound() {
        return valueMilliseconds > 999; // 1" and above
    }

    public float getStopsDifference(ShutterSpeed relative) {
        return (float) (Math.log(valueMilliseconds / relative.getDurationMilliseconds()) / Math.log(2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShutterSpeed that = (ShutterSpeed) o;
        return Float.compare(valueMilliseconds, that.valueMilliseconds) == 0 && Objects.equals(notation, that.notation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueMilliseconds, notation);
    }
}
