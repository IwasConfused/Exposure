package io.github.mortuusars.exposure.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.core.ExposureFrameTag;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.frame.Photographer;
import io.github.mortuusars.exposure.core.ChromaChannel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public record ExposureFrame(ExposureIdentifier identifier,
                            ExposureType type,
                            Photographer photographer,
                            List<EntityInFrame> entitiesInFrame,
                            CustomData additionalData) {
    public static final ExposureFrame EMPTY = new ExposureFrame(
            ExposureIdentifier.EMPTY,
            ExposureType.COLOR,
            Photographer.EMPTY,
            Collections.emptyList(),
            CustomData.EMPTY);

    public static final Codec<ExposureFrame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExposureIdentifier.CODEC.fieldOf("id").forGetter(ExposureFrame::identifier),
                    ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(ExposureFrame::type),
                    Photographer.CODEC.optionalFieldOf("photographer", Photographer.EMPTY).forGetter(ExposureFrame::photographer),
                    EntityInFrame.CODEC.listOf(0, 16).optionalFieldOf("captured_entities", Collections.emptyList()).forGetter(ExposureFrame::entitiesInFrame),
                    CustomData.CODEC.optionalFieldOf("additional_data", CustomData.EMPTY).forGetter(ExposureFrame::additionalData))
            .apply(instance, ExposureFrame::new));

    @SuppressWarnings("deprecation")
    public static final StreamCodec<FriendlyByteBuf, ExposureFrame> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull ExposureFrame decode(FriendlyByteBuf buffer) {
            return new ExposureFrame(
                    ExposureIdentifier.STREAM_CODEC.decode(buffer),
                    ExposureType.STREAM_CODEC.decode(buffer),
                    Photographer.STREAM_CODEC.decode(buffer),
                    EntityInFrame.STREAM_CODEC.apply(ByteBufCodecs.list(16)).decode(buffer),
                    CustomData.STREAM_CODEC.decode(buffer));
        }

        public void encode(FriendlyByteBuf buffer, ExposureFrame frame) {
            ExposureIdentifier.STREAM_CODEC.encode(buffer, frame.identifier());
            ExposureType.STREAM_CODEC.encode(buffer, frame.type());
            Photographer.STREAM_CODEC.encode(buffer, frame.photographer);
            EntityInFrame.STREAM_CODEC.apply(ByteBufCodecs.list(16)).encode(buffer, frame.entitiesInFrame());
            CustomData.STREAM_CODEC.encode(buffer, frame.additionalData());
        }
    };

    /**
     * Creates an object that has data common to all objects in a provided list.
     * If value is not common to all objects - default value will be used.
     * Currently used in Chromatic Sheets to create a common data object from 3 layers.
     */
    public static ExposureFrame intersect(ExposureIdentifier identifier, List<ExposureFrame> objects) {
        Mutable result = EMPTY.toMutable().setIdentifier(identifier);

        if (objects.isEmpty()) {
            return result.toImmutable();
        }

        result.setType(getCommonValueOrDefault(objects, ExposureFrame::type));
        result.setPhotographer(getCommonValueOrDefault(objects, ExposureFrame::photographer));

        List<EntityInFrame> commonEntitiesInFrame = objects.stream()
                .map(ExposureFrame::entitiesInFrame)
                .map(HashSet::new)
                .reduce((set1, set2) -> {
                    set1.retainAll(set2);
                    return set1;
                })
                .map(ArrayList::new)
                .orElse(new ArrayList<>());
        result.setEntitiesInFrame(commonEntitiesInFrame);

        CompoundTag mergedTag = objects.stream()
                .map(f -> f.additionalData.copyTag())
                .reduce(new CompoundTag(), CompoundTag::merge);
        result.setAdditionalDataTag(mergedTag);

        return result.toImmutable();
    }

    private static <V> V getCommonValueOrDefault(List<ExposureFrame> objects, Function<ExposureFrame, V> propertyGetter) {
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
    public CompoundTag getAdditionalDataTagForReading() {
        //noinspection deprecation
        return additionalData().getUnsafe();
    }

    public boolean isFromFile() {
        return getAdditionalDataTagForReading().getBoolean(ExposureFrameTag.FROM_FILE);
    }

    public boolean isChromatic() {
        return getAdditionalDataTagForReading().getBoolean(ExposureFrameTag.CHROMATIC);
    }

    public Optional<ChromaChannel> getChromaticChannel() {
        return ChromaChannel.fromString(getAdditionalDataTagForReading().getString(ExposureFrameTag.CHROMATIC_CHANNEL));
    }

    public boolean wasTakenWithChromaticFilter() {
        return getChromaticChannel().isPresent();
    }

    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static class Mutable {
        private ExposureIdentifier identifier;
        private ExposureType type;
        private Photographer photographer;
        private List<EntityInFrame> entitiesInFrame;
        private CompoundTag additionalData;

        public Mutable(ExposureFrame photographData) {
            this.identifier = photographData.identifier();
            this.type = photographData.type();
            this.photographer = photographData.photographer();
            this.entitiesInFrame = new ArrayList<>(photographData.entitiesInFrame());
            this.additionalData = photographData.additionalData().copyTag();
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

        public Mutable setPhotographer(LivingEntity entity) {
            this.photographer = new Photographer(entity);
            return this;
        }

        public List<EntityInFrame> getEntitiesInFrame() {
            return entitiesInFrame;
        }

        public Mutable setEntitiesInFrame(List<EntityInFrame> entitiesInFrame) {
            this.entitiesInFrame = entitiesInFrame;
            return this;
        }

        public CompoundTag getAdditionalDataTag() {
            return additionalData;
        }

        public Mutable setAdditionalDataTag(CompoundTag additionalData) {
            this.additionalData = additionalData;
            return this;
        }

        public Mutable updateAdditionalData(Consumer<CompoundTag> updater) {
            updater.accept(additionalData);
            return this;
        }

        public Mutable setChromatic(boolean chromatic) {
            return updateAdditionalData(tag -> tag.putBoolean(ExposureFrameTag.CHROMATIC, chromatic));
        }

        public ExposureFrame toImmutable() {
            return new ExposureFrame(
                    this.identifier,
                    this.type,
                    this.photographer,
                    this.entitiesInFrame,
                    CustomData.of(this.additionalData));
        }
    }
}
