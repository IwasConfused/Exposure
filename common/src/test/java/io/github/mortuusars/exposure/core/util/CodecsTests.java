package io.github.mortuusars.exposure.core.util;

import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.util.Codecs;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CodecsTests {
    @Test
    void intArrayCodecJson() {
        int[] array = new int[]{-1, 0, 42, Integer.MAX_VALUE};

        JsonArray jsonArray = Codecs.intArrayCodec(0, 256).encodeStart(JsonOps.INSTANCE, array).getOrThrow().getAsJsonArray();

        assertEquals(-1, jsonArray.get(0).getAsInt());
        assertEquals(0, jsonArray.get(1).getAsInt());
        assertEquals(42, jsonArray.get(2).getAsInt());
        assertEquals(Integer.MAX_VALUE, jsonArray.get(3).getAsInt());
        assertEquals(4, jsonArray.size());
    }

    @Test
    void intArrayCodecNbt() {
        int[] array = new int[]{-1, 0, 42, Integer.MAX_VALUE};

        IntArrayTag tag = ((IntArrayTag) Codecs.intArrayCodec(0, 256).encodeStart(NbtOps.INSTANCE, array).getOrThrow());

        assertEquals(-1, tag.get(0).getAsInt());
        assertEquals(0, tag.get(1).getAsInt());
        assertEquals(42, tag.get(2).getAsInt());
        assertEquals(Integer.MAX_VALUE, tag.get(3).getAsInt());
        assertEquals(4, tag.size());
    }
}
