package io.github.mortuusars.exposure;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ExtraData extends CompoundTag {

    public static final Entry<Integer> LIGHT_LEVEL = new Entry<>("light_level", CompoundTag::getInt, CompoundTag::putInt);

    public <T> Optional<T> getEntry(Entry<T> entry) {
        return contains(entry.key()) ? Optional.ofNullable(entry.getter().apply(this, entry.key())) : Optional.empty();
    }

    public <T> void setEntry(Entry<T> entry, T value) {
        entry.setter().accept(this, entry.key(), value);
    }

    public record Entry<T>(String key, BiFunction<CompoundTag, String, T> getter, TriConsumer<CompoundTag, String, T> setter) {
    }
}
