package io.github.mortuusars.exposure.core.frame;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.CustomData;

public class FrameTag {
    public static final String FROM_FILE = "from_file";
    public static final String CHROMATIC = "chromatic";

    public static final String CHROMATIC_CHANNEL = "chromatic_channel";
    public static final String SHUTTER_SPEED_MS = "shutter_speed_ms";
    public static final String FOCAL_LENGTH = "focal_length";
    public static final String TIMESTAMP = "timestamp";

    public static final String FLASH = "flash";
    public static final String SELFIE = "selfie";
    public static final String POSITION = "pos";
    public static final String PITCH = "pitch";
    public static final String YAW = "yaw";
    public static final String LIGHT_LEVEL = "light_level";
    public static final String DAY_TIME = "day_time";
    public static final String DIMENSION = "dimension";
    public static final String BIOME = "biome";
    public static final String WEATHER = "weather";
    public static final String IN_CAVE = "in_cave";
    public static final String UNDERWATER = "underwater";

    // --

    public static final Codec<FrameTag> CODEC = CustomData.CODEC.xmap(FrameTag::new, FrameTag::getData);
    @SuppressWarnings("deprecation")
    public static final StreamCodec<ByteBuf, FrameTag> STREAM_CODEC = CustomData.STREAM_CODEC.map(FrameTag::new, FrameTag::getData);

    public static final FrameTag EMPTY = new FrameTag(CustomData.EMPTY);

    private final CustomData data;

    public FrameTag(CustomData data) {
        this.data = data;
    }

    public static FrameTag of(CompoundTag tag) {
        return new FrameTag(CustomData.of(tag));
    }

    public CustomData getData() {
        return data;
    }
}
