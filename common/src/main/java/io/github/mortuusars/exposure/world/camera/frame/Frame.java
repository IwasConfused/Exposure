package io.github.mortuusars.exposure.world.camera.frame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public record Frame(ExposureIdentifier exposureIdentifier,
                    ExposureType type,
                    Photographer photographer,
                    List<EntityInFrame> entitiesInFrame,
                    CustomData tag) {
    public static final String PROJECTED = "projected";
    public static final String CHROMATIC = "chromatic";

    public static final String COLOR_CHANNEL = "color_channel";
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

    public static final Frame EMPTY = new Frame(
            ExposureIdentifier.EMPTY,
            ExposureType.COLOR,
            Photographer.EMPTY,
            Collections.emptyList(),
            CustomData.EMPTY);

    public static final Codec<Frame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExposureIdentifier.CODEC.fieldOf("exposure").forGetter(Frame::exposureIdentifier),
                    ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(Frame::type),
                    Photographer.CODEC.optionalFieldOf("photographer", Photographer.EMPTY).forGetter(Frame::photographer),
                    EntityInFrame.CODEC.listOf(0, 16).optionalFieldOf("captured_entities", Collections.emptyList()).forGetter(Frame::entitiesInFrame),
                    CustomData.CODEC.optionalFieldOf("tag", CustomData.EMPTY).forGetter(Frame::tag))
            .apply(instance, Frame::new));

    public static final StreamCodec<FriendlyByteBuf, Frame> STREAM_CODEC = new StreamCodec<>() {
        @SuppressWarnings("deprecation")
        public @NotNull Frame decode(FriendlyByteBuf buffer) {
            return new Frame(
                    ExposureIdentifier.STREAM_CODEC.decode(buffer),
                    ExposureType.STREAM_CODEC.decode(buffer),
                    Photographer.STREAM_CODEC.decode(buffer),
                    EntityInFrame.STREAM_CODEC.apply(ByteBufCodecs.list(16)).decode(buffer),
                    CustomData.STREAM_CODEC.decode(buffer));
        }

        @SuppressWarnings("deprecation")
        public void encode(FriendlyByteBuf buffer, Frame frame) {
            ExposureIdentifier.STREAM_CODEC.encode(buffer, frame.exposureIdentifier());
            ExposureType.STREAM_CODEC.encode(buffer, frame.type());
            Photographer.STREAM_CODEC.encode(buffer, frame.photographer);
            EntityInFrame.STREAM_CODEC.apply(ByteBufCodecs.list(16)).encode(buffer, frame.entitiesInFrame());
            CustomData.STREAM_CODEC.encode(buffer, frame.tag());
        }
    };

    /**
     * Creates a frame that has data common to all frames in a provided list.
     * If value is not common to all objects - default value will be used.
     * Currently used in Chromatic Sheets to create a common data object from 3 layers.
     */
    public static Frame intersect(ExposureIdentifier identifier, List<Frame> frames) {
        Mutable result = EMPTY.toMutable().setIdentifier(identifier);

        if (frames.isEmpty()) {
            return result.toImmutable();
        }

        result.setType(getCommonValueOrDefault(frames, Frame::type));
        result.setPhotographer(getCommonValueOrDefault(frames, Frame::photographer));

        List<EntityInFrame> commonEntitiesInFrame = frames.stream()
                .map(Frame::entitiesInFrame)
                .map(HashSet::new)
                .reduce((set1, set2) -> {
                    set1.retainAll(set2);
                    return set1;
                })
                .map(ArrayList::new)
                .orElse(new ArrayList<>());
        result.setEntitiesInFrame(commonEntitiesInFrame);

        CompoundTag mergedTag = frames.stream()
                .map(f -> f.tag.copyTag())
                .reduce(new CompoundTag(), CompoundTag::merge);
        result.setTag(mergedTag);

        return result.toImmutable();
    }

    private static <V> V getCommonValueOrDefault(List<Frame> objects, Function<Frame, V> propertyGetter) {
        V referenceValue = propertyGetter.apply(objects.getFirst());
        return objects.stream().allMatch(data -> propertyGetter.apply(data) == referenceValue) ? referenceValue : propertyGetter.apply(EMPTY);
    }

    private static <T, V> V getCommonValueOrElse(List<T> objects, Function<T, V> propertyGetter, V fallbackValue) {
        V referenceValue = propertyGetter.apply(objects.getFirst());
        return objects.stream().allMatch(data -> propertyGetter.apply(data) == referenceValue) ? referenceValue : fallbackValue;
    }

    public boolean isTakenBy(LivingEntity entity) {
        return photographer().matches(entity);
    }

    /**
     * Do not modify the tag here! It may cause unwanted side effects.
     */
    @SuppressWarnings("deprecation")
    public CompoundTag getAdditionalDataTagForReading() {
        return tag().getUnsafe();
    }

    public boolean isFromFile() {
        return getAdditionalDataTagForReading().getBoolean(PROJECTED);
    }

    public boolean isChromatic() {
        return getAdditionalDataTagForReading().getBoolean(CHROMATIC);
    }

    public Optional<ColorChannel> getColorChannel() {
        return ColorChannel.fromString(getAdditionalDataTagForReading().getString(COLOR_CHANNEL));
    }

    public boolean wasTakenWithChromaticFilter() {
        return getColorChannel().isPresent();
    }

    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static class Mutable {
        private ExposureIdentifier identifier;
        private ExposureType type;
        private Photographer photographer;
        private List<EntityInFrame> entitiesInFrame;
        private CompoundTag tag;

        public Mutable(Frame photographData) {
            this.identifier = photographData.exposureIdentifier();
            this.type = photographData.type();
            this.photographer = photographData.photographer();
            this.entitiesInFrame = new ArrayList<>(photographData.entitiesInFrame());
            this.tag = photographData.tag().copyTag();
        }

        public ExposureIdentifier getIdentifier() {
            return identifier;
        }

        public Mutable setIdentifier(ExposureIdentifier identifier) {
            this.identifier = identifier;
            return this;
        }

        public ExposureType getType() {
            return type;
        }

        public Mutable setType(ExposureType type) {
            this.type = type;
            return this;
        }

        public Photographer getPhotographer() {
            return photographer;
        }

        public Mutable setPhotographer(@NotNull Photographer photographer) {
            this.photographer = photographer;
            return this;
        }

        public List<EntityInFrame> getEntitiesInFrame() {
            return entitiesInFrame;
        }

        public Mutable setEntitiesInFrame(List<EntityInFrame> entitiesInFrame) {
            this.entitiesInFrame = entitiesInFrame;
            return this;
        }

        public CompoundTag getTag() {
            return tag;
        }

        public Mutable setTag(CompoundTag tag) {
            this.tag = tag;
            return this;
        }

        public Mutable updateTag(Consumer<CompoundTag> updater) {
            updater.accept(tag);
            return this;
        }

        public Mutable setChromatic(boolean chromatic) {
            return updateTag(tag -> tag.putBoolean(CHROMATIC, chromatic));
        }

        public Frame toImmutable() {
            return new Frame(
                    this.identifier,
                    this.type,
                    this.photographer,
                    this.entitiesInFrame,
                    CustomData.of(this.tag));
        }
    }
}
