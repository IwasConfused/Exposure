package io.github.mortuusars.exposure.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Extension of CompoundTag to allow better type safety. <br>
 * {@link Entry} is meant to be stored in a static final field in appropriate places.
 */
public class ExtraData extends CompoundTag {
    public static final Codec<ExtraData> CODEC = CompoundTag.CODEC.xmap(ExtraData::new, data -> data);
    public static final StreamCodec<ByteBuf, ExtraData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(ExtraData::new, data -> data);

    public static final ExtraData EMPTY = new ExtraData();
    private final Map<String, Tag> tags;

    protected ExtraData(Map<String, Tag> tags) {
        super(tags);
        this.tags = tags;
    }

    public ExtraData() {
        this(new HashMap<>());
    }

    public ExtraData(CompoundTag tag) {
        this(new HashMap<>());
        merge(tag);
    }

    public <T> Optional<T> get(@NotNull Entry<T> entry) {
        if (!contains(entry.key())) return Optional.empty();
        try {
            return Optional.ofNullable(entry.getter().apply(this, entry.key()));
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public <T> T getOrDefault(@NotNull Entry<T> entry, T defaultValue) {
        if (!contains(entry.key())) return defaultValue;
        try {
            @Nullable T value = entry.getter().apply(this, entry.key());
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", e.getMessage());
            return defaultValue;
        }
    }

    public <T> void put(Entry<T> entry, @NotNull T value) {
        Preconditions.checkNotNull(value, "value");
        entry.setter().accept(this, entry.key(), value);
    }

    public <T> void remove(Entry<T> entry) {
        remove(entry.key());
    }

    // --

    @Override
    public @NotNull ExtraData copy() {
        Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
        return new ExtraData(map);
    }

    @Override
    public @NotNull ExtraData merge(CompoundTag other) {
        for (String key : other.getAllKeys()) {
            Tag tag = other.get(key);
            assert tag != null;
            if (tag.getId() == 10) {
                if (this.contains(key, 10)) {
                    CompoundTag compoundTag = this.getCompound(key);
                    compoundTag.merge((CompoundTag) tag);
                } else {
                    this.put(key, tag.copy());
                }
            } else {
                this.put(key, tag.copy());
            }
        }

        return this;
    }

    // --

    public record Entry<T>(String key, BiFunction<ExtraData, String, @Nullable T> getter,
                           TriConsumer<ExtraData, String, T> setter) {
        public static Entry<String> string(String key) {
            return new Entry<>(key, ExtraData::getString, ExtraData::putString);
        }

        public static Entry<Boolean> bool(String key) {
            return new Entry<>(key, ExtraData::getBoolean, ExtraData::putBoolean);
        }

        public static Entry<Integer> intVal(String key) {
            return new Entry<>(key, ExtraData::getInt, ExtraData::putInt);
        }

        public static Entry<Long> longVal(String key) {
            return new Entry<>(key, ExtraData::getLong, ExtraData::putLong);
        }

        public static Entry<Float> floatVal(String key) {
            return new Entry<>(key, ExtraData::getFloat, ExtraData::putFloat);
        }

        public static Entry<Double> doubleVal(String key) {
            return new Entry<>(key, ExtraData::getDouble, ExtraData::putDouble);
        }

        public static <T extends StringRepresentable> Entry<T> stringRepresentable(String key, Function<String, @Nullable T> deserializeFunction) {
            return new Entry<>(key,
                    (data, k) -> deserializeFunction.apply(data.getString(k)),
                    (data, k, value) -> data.putString(k, value.getSerializedName()));
        }

        public static Entry<Vec3> vec3(String key) {
            return new Entry<>(key,
                    (data, k) -> {
                        ListTag pos = data.getList(k, DoubleTag.TAG_DOUBLE);
                        return new Vec3(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
                    },
                    (data, k, value) -> {
                        ListTag pos = new ListTag();
                        pos.add(DoubleTag.valueOf(value.x()));
                        pos.add(DoubleTag.valueOf(value.y()));
                        pos.add(DoubleTag.valueOf(value.z()));
                        data.put(k, pos);
                    });
        }

        public static Entry<ResourceLocation> resourceLocation(String key) {
            return new Entry<>(key,
                    (data, k) -> ResourceLocation.parse(data.getString(k)),
                    (data, k, value) -> data.putString(k, value.toString()));
        }

        public static <T> Entry<List<T>> list(String key, int tagType, Function<Tag, T> extractFunc, Function<T, Tag> packFunc) {
            return new Entry<>(key,
                    (data, k) -> data.getList(k, tagType).stream()
                            .map(extractFunc)
                            .toList(),
                    (data, k, value) -> {
                        ListTag list = new ListTag();
                        list.addAll(value.stream()
                                .map(packFunc)
                                .toList());
                        data.put(k, list);
                    });
        }

        public static <T> Entry<List<T>> stringBasedList(String key, Function<String, T> extractFunc, Function<T, String> packFunc) {
            return list(key, Tag.TAG_STRING, tag -> extractFunc.apply(tag.getAsString()), value -> StringTag.valueOf(packFunc.apply(value)));
        }
    }
}
