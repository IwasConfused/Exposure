package io.github.mortuusars.exposure.warehouse;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.util.ByteArrayUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class ExposureData extends SavedData {
    public static final Codec<ExposureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("width").forGetter(ExposureData::getWidth),
            Codec.INT.fieldOf("height").forGetter(ExposureData::getHeight),
            ByteArrayUtils.CODEC.fieldOf("pixels").forGetter(ExposureData::getPixels),
            ColorPalette.CODEC.optionalFieldOf("palette", ColorPalette.MAP_COLORS).forGetter(ExposureData::getPalette),
            ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(ExposureData::getType),
            Codec.STRING.optionalFieldOf("creator", "").forGetter(ExposureData::getCreator),
            Codec.LONG.optionalFieldOf("timestamp", 1645494000L).forGetter(ExposureData::getTimestamp),
            Codec.BOOL.optionalFieldOf("from_file", false).forGetter(ExposureData::isFromFile),
            CompoundTag.CODEC.optionalFieldOf("extra_data", new CompoundTag()).forGetter(ExposureData::getExtraData),
            Codec.BOOL.optionalFieldOf("was_printed", false).forGetter(ExposureData::hasBeenPrinted)
    ).apply(instance, ExposureData::new));

    public static final StreamCodec<ByteBuf, ExposureData> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull ExposureData decode(ByteBuf buffer) {
            return new ExposureData(
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.byteArray(2048 * 2048).decode(buffer),
                    ColorPalette.STREAM_CODEC.decode(buffer),
                    ExposureType.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.VAR_LONG.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.COMPOUND_TAG.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer));
        }

        public void encode(ByteBuf buffer, ExposureData data) {
            ByteBufCodecs.VAR_INT.encode(buffer, data.getWidth());
            ByteBufCodecs.VAR_INT.encode(buffer, data.getWidth());
            ByteBufCodecs.byteArray(2048*2048).encode(buffer, data.getPixels());
            ColorPalette.STREAM_CODEC.encode(buffer, data.getPalette());
            ExposureType.STREAM_CODEC.encode(buffer, data.getType());
            ByteBufCodecs.STRING_UTF8.encode(buffer, data.getCreator());
            ByteBufCodecs.VAR_LONG.encode(buffer, data.getTimestamp());
            ByteBufCodecs.BOOL.encode(buffer, data.isFromFile());
            ByteBufCodecs.COMPOUND_TAG.encode(buffer, data.getExtraData());
            ByteBufCodecs.BOOL.encode(buffer, data.hasBeenPrinted());
        }
    };

    public static final ExposureData EMPTY = new ExposureData(1, 1, new byte[]{0}, ColorPalette.MAP_COLORS,
            ExposureType.COLOR, "", 0, false, new CompoundTag(), false);

    private final int width;
    private final int height;
    private final byte[] pixels;
    private final ColorPalette palette;
    private final ExposureType type;
    private final String creator;
    private final long timestamp;
    private final boolean fromFile;
    private final CompoundTag extraData;
    private boolean wasPrinted;

    public ExposureData(int width, int height, byte[] pixels, ColorPalette palette, ExposureType type,
                        String creator, long timestamp, boolean fromFile, CompoundTag extraData, boolean wasPrinted) {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative. %s", this);
        Preconditions.checkArgument(height >= 0, "Height cannot be negative. %s ", this);
        Preconditions.checkArgument(pixels.length == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixels.length, width, height, width * height);
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.palette = palette;
        this.type = type;
        this.creator = creator;
        this.timestamp = timestamp;
        this.fromFile = fromFile;
        this.extraData = extraData;
        this.wasPrinted = wasPrinted;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        DataResult<Tag> encodingResult = CODEC.encode(this, NbtOps.INSTANCE, tag);
        if (encodingResult.isSuccess()) {
            Tag encodedTag = encodingResult.getOrThrow();
            if (encodedTag instanceof CompoundTag encodedCompoundTag)
                return encodedCompoundTag;
            else {
                Exposure.LOGGER.error("Cannot save ExposureSavedData: '{}'. Encoded tag is not CompoundTag but a {}",
                        this, encodedTag.getType());
            }
        }
        encodingResult.error().ifPresent(error -> Exposure.LOGGER.error("Cannot save FramesHistory: {}", error.message()));

        return tag;
    }

    public static SavedData.Factory<ExposureData> factory() {
        return new SavedData.Factory<>(() -> {
            throw new IllegalStateException("Should never create an empty exposure saved encodedValue");
        }, ExposureData::load, null);
    }

    public static ExposureData load(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        return CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getPixels() {
        return pixels;
    }

    public byte getPixel(int x, int y) {
        return pixels[y * width + x];
    }

    public ColorPalette getPalette() {
        return palette;
    }

    public ExposureType getType() {
        return type;
    }

    public String getCreator() {
        return creator;
    }

    /**
     * Timestamp in unix seconds.
     */
    public long getTimestamp() {
        return timestamp;
    }

    public boolean isFromFile() {
        return fromFile;
    }

    public CompoundTag getExtraData() {
        return extraData;
    }

    public boolean hasBeenPrinted() {
        return wasPrinted;
    }

    public void markAsPrinted() {
        if (!this.equals(EMPTY)) {
            wasPrinted = true;
            setDirty();
        }
    }
}
