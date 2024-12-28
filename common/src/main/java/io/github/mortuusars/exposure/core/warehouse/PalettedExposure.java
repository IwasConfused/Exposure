package io.github.mortuusars.exposure.core.warehouse;

import com.google.common.base.Function;
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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class PalettedExposure extends SavedData {
    public static final Codec<PalettedExposure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("width").forGetter(PalettedExposure::getWidth),
            Codec.INT.fieldOf("height").forGetter(PalettedExposure::getHeight),
            ByteArrayUtils.CODEC.fieldOf("pixels").forGetter(PalettedExposure::getPixels),
            ColorPalette.CODEC.optionalFieldOf("palette", ColorPalette.MAP_COLORS).forGetter(PalettedExposure::getPalette),
            Tag.CODEC.optionalFieldOf("metadata", Tag.EMPTY).forGetter(PalettedExposure::getTag)
    ).apply(instance, PalettedExposure::new));

    public static final StreamCodec<ByteBuf, PalettedExposure> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PalettedExposure::getWidth,
            ByteBufCodecs.VAR_INT, PalettedExposure::getHeight,
            ByteBufCodecs.byteArray(2048 * 2048), PalettedExposure::getPixels,
            ColorPalette.STREAM_CODEC, PalettedExposure::getPalette,
            Tag.STREAM_CODEC, PalettedExposure::getTag,
            PalettedExposure::new
    );

    public static final PalettedExposure EMPTY = new PalettedExposure(
            1, 1, new byte[]{0}, ColorPalette.MAP_COLORS, Tag.EMPTY);

    private final int width;
    private final int height;
    private final byte[] pixels;
    private final ColorPalette palette;
    private Tag tag;

    public PalettedExposure(int width, int height, byte[] pixels, ColorPalette palette, Tag tag) {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative. %s", this);
        Preconditions.checkArgument(height >= 0, "Height cannot be negative. %s ", this);
        Preconditions.checkArgument(pixels.length == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixels.length, width, height, width * height);
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.palette = palette;
        this.tag = tag;
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

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
        setDirty();
    }

    public void updateTag(Function<Tag, Tag> updateFunction) {
        setTag(updateFunction.apply(this.tag));
    }

    // --

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        DataResult<net.minecraft.nbt.Tag> encodingResult = CODEC.encode(this, NbtOps.INSTANCE, tag);
        if (encodingResult.isSuccess()) {
            net.minecraft.nbt.Tag encodedTag = encodingResult.getOrThrow();
            if (encodedTag instanceof CompoundTag encodedCompoundTag)
                return encodedCompoundTag;
            else {
                Exposure.LOGGER.error("Cannot save PalettedExposure: '{}'. Encoded tag is not CompoundTag but a {}",
                        this, encodedTag.getType());
            }
        }
        encodingResult.error().ifPresent(error -> Exposure.LOGGER.error("Cannot save PalettedExposure: {}", error.message()));

        return tag;
    }

    public static SavedData.Factory<PalettedExposure> factory() {
        return new SavedData.Factory<>(() -> {
            throw new IllegalStateException("Should never create an empty exposure saved data");
        }, PalettedExposure::load, null);
    }

    public static PalettedExposure load(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        return CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
    }

    public record Tag(ExposureType type,
                      String creator,
                      long unixTimestamp,
                      boolean isFromFile,
                      boolean wasPrinted) {
        public static final Codec<Tag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(Tag::type),
                Codec.STRING.optionalFieldOf("creator", "").forGetter(Tag::creator),
                Codec.LONG.optionalFieldOf("timestamp", 1645494000L).forGetter(Tag::unixTimestamp),
                Codec.BOOL.optionalFieldOf("from_file", false).forGetter(Tag::isFromFile),
                Codec.BOOL.optionalFieldOf("was_printed", false).forGetter(Tag::wasPrinted)
        ).apply(instance, Tag::new));

        public static final StreamCodec<ByteBuf, Tag> STREAM_CODEC = new StreamCodec<>() {
            public @NotNull PalettedExposure.Tag decode(ByteBuf buffer) {
                return new Tag(
                        ExposureType.STREAM_CODEC.decode(buffer),
                        ByteBufCodecs.STRING_UTF8.decode(buffer),
                        ByteBufCodecs.VAR_LONG.decode(buffer),
                        ByteBufCodecs.BOOL.decode(buffer),
                        ByteBufCodecs.BOOL.decode(buffer));
            }

            public void encode(ByteBuf buffer, Tag data) {
                ExposureType.STREAM_CODEC.encode(buffer, data.type());
                ByteBufCodecs.STRING_UTF8.encode(buffer, data.creator());
                ByteBufCodecs.VAR_LONG.encode(buffer, data.unixTimestamp());
                ByteBufCodecs.BOOL.encode(buffer, data.isFromFile());
                ByteBufCodecs.BOOL.encode(buffer, data.wasPrinted());
            }
        };

        public static final Tag EMPTY = new Tag(ExposureType.COLOR, "", 0, false, false);

        public Tag withType(ExposureType type) {
            return new Tag(type, creator, unixTimestamp, isFromFile, wasPrinted);
        }

        public Tag withCreator(String creator) {
            return new Tag(type, creator, unixTimestamp, isFromFile, wasPrinted);
        }

        public Tag withTimestamp(long unixTimestamp) {
            return new Tag(type, creator, unixTimestamp, isFromFile, wasPrinted);
        }

        public Tag withFromFileSetTo(boolean isFromFile) {
            return new Tag(type, creator, unixTimestamp, isFromFile, wasPrinted);
        }

        public Tag withWasPrintedSetTo(boolean wasPrinted) {
            return new Tag(type, creator, unixTimestamp, isFromFile, wasPrinted);
        }

        public Tag setPrinted() {
            return new Tag(type, creator, unixTimestamp, isFromFile, true);
        }
    }
}
