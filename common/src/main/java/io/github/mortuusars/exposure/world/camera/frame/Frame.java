package io.github.mortuusars.exposure.world.camera.frame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public record Frame(ExposureIdentifier identifier,
                    ExposureType type,
                    Photographer photographer,
                    List<EntityInFrame> entitiesInFrame,
                    ExtraData extraData) {
    public static final ExtraData.Entry<Boolean> PROJECTED = ExtraData.Entry.bool("projected");
    public static final ExtraData.Entry<Boolean> CHROMATIC = ExtraData.Entry.bool("chromatic");

    public static final ExtraData.Entry<ColorChannel> COLOR_CHANNEL =
            ExtraData.Entry.stringRepresentable("color_channel", ColorChannel::fromStringOrThrow);
    public static final ExtraData.Entry<ShutterSpeed> SHUTTER_SPEED =
            ExtraData.Entry.stringRepresentable("shutter_speed", ShutterSpeed::new);

    public static final ExtraData.Entry<Long> TIMESTAMP = ExtraData.Entry.longVal("timestamp");
    public static final ExtraData.Entry<Integer> FOCAL_LENGTH = ExtraData.Entry.intVal("focal_length");
    public static final ExtraData.Entry<Boolean> FLASH = ExtraData.Entry.bool("flash");
    public static final ExtraData.Entry<Boolean> SELFIE = ExtraData.Entry.bool("selfie");

    public static final ExtraData.Entry<Vec3> POSITION = ExtraData.Entry.vec3("pos");
    public static final ExtraData.Entry<Float> PITCH = ExtraData.Entry.floatVal("pitch");
    public static final ExtraData.Entry<Float> YAW = ExtraData.Entry.floatVal("yaw");
    public static final ExtraData.Entry<Integer> LIGHT_LEVEL = ExtraData.Entry.intVal("light_level");
    public static final ExtraData.Entry<Integer> DAY_TIME = ExtraData.Entry.intVal("day_time");
    public static final ExtraData.Entry<ResourceLocation> DIMENSION = ExtraData.Entry.resourceLocation("dimension");
    public static final ExtraData.Entry<ResourceLocation> BIOME = ExtraData.Entry.resourceLocation("biome");
    public static final ExtraData.Entry<String> WEATHER = ExtraData.Entry.string("weather");
    public static final ExtraData.Entry<Boolean> IN_CAVE = ExtraData.Entry.bool("in_cave");
    public static final ExtraData.Entry<Boolean> UNDERWATER = ExtraData.Entry.bool("underwater");
    public static final ExtraData.Entry<List<ResourceLocation>> STRUCTURES = ExtraData.Entry.stringBasedList("structures",
            ResourceLocation::parse, ResourceLocation::toString);
    // --

    public static final Frame EMPTY = new Frame(
            ExposureIdentifier.EMPTY,
            ExposureType.COLOR,
            Photographer.EMPTY,
            Collections.emptyList(),
            ExtraData.EMPTY);

    public static final Codec<Frame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExposureIdentifier.CODEC.fieldOf("identifier").forGetter(Frame::identifier),
                    ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(Frame::type),
                    Photographer.CODEC.optionalFieldOf("photographer", Photographer.EMPTY).forGetter(Frame::photographer),
                    EntityInFrame.CODEC.listOf(0, 16).optionalFieldOf("entities_in_frame", Collections.emptyList()).forGetter(Frame::entitiesInFrame),
                    ExtraData.CODEC.optionalFieldOf("extra_data", ExtraData.EMPTY).forGetter(Frame::extraData))
            .apply(instance, Frame::new));

    public static final StreamCodec<FriendlyByteBuf, Frame> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull Frame decode(FriendlyByteBuf buffer) {
            return new Frame(
                    ExposureIdentifier.STREAM_CODEC.decode(buffer),
                    ExposureType.STREAM_CODEC.decode(buffer),
                    Photographer.STREAM_CODEC.decode(buffer),
                    EntityInFrame.STREAM_CODEC.apply(ByteBufCodecs.list(16)).decode(buffer),
                    ExtraData.STREAM_CODEC.decode(buffer));
        }

        public void encode(FriendlyByteBuf buffer, Frame frame) {
            ExposureIdentifier.STREAM_CODEC.encode(buffer, frame.identifier());
            ExposureType.STREAM_CODEC.encode(buffer, frame.type());
            Photographer.STREAM_CODEC.encode(buffer, frame.photographer);
            EntityInFrame.STREAM_CODEC.apply(ByteBufCodecs.list(16)).encode(buffer, frame.entitiesInFrame());
            ExtraData.STREAM_CODEC.encode(buffer, frame.extraData());
        }
    };

    public static Mutable create() {
        return new Mutable(EMPTY);
    }

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

        ExtraData mergedTag = frames.stream()
                .map(f -> f.extraData.copy())
                .reduce(new ExtraData(), ExtraData::merge);
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
    public ExtraData getExtraDataForReading() {
        return extraData();
    }

    public boolean isProjected() {
        return getExtraDataForReading().get(PROJECTED).orElse(false);
    }

    public boolean isChromatic() {
        return getExtraDataForReading().get(CHROMATIC).orElse(false);
    }

    public Optional<ColorChannel> getColorChannel() {
        return getExtraDataForReading().get(COLOR_CHANNEL);
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
        private ExtraData tag;

        public Mutable(Frame photographData) {
            this.identifier = photographData.identifier();
            this.type = photographData.type();
            this.photographer = photographData.photographer();
            this.entitiesInFrame = new ArrayList<>(photographData.entitiesInFrame());
            this.tag = photographData.extraData().copy();
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

        public ExtraData getTag() {
            return tag;
        }

        public Mutable setTag(ExtraData tag) {
            this.tag = tag;
            return this;
        }

        public Mutable updateExtraData(Consumer<ExtraData> updater) {
            updater.accept(tag);
            return this;
        }

        public <T> Mutable addExtraData(ExtraData.Entry<T> entry, T value) {
            tag.put(entry, value);
            return this;
        }

        public Mutable setChromatic(boolean chromatic) {
            return updateExtraData(tag -> tag.put(CHROMATIC, chromatic));
        }

        public Frame toImmutable() {
            return new Frame(
                    this.identifier,
                    this.type,
                    this.photographer,
                    this.entitiesInFrame,
                    this.tag);
        }
    }
}
