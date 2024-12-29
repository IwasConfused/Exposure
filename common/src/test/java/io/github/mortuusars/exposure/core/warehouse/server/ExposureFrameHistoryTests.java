package io.github.mortuusars.exposure.core.warehouse.server;

import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ExposureFrameHistoryTests {
    @Test
    void encoding() {
        UUID randomUUID = UUID.randomUUID();

        ExposureFrameHistory history = new ExposureFrameHistory(new HashMap<>());
        history.add(randomUUID, ExposureFrame.EMPTY.toMutable().setIdentifier(ExposureIdentifier.id("test")).toImmutable());

        CompoundTag tag = history.save(new CompoundTag(), HolderLookup.Provider.create(Stream.of()));

        String expected = "{" + randomUUID + ":[{id:\"test\"}]}";
        assertEquals(expected, tag.toString());
    }

    @Test
    void decoding() {
        UUID randomUUID = UUID.randomUUID();

        ExposureFrame frame = ExposureFrame.EMPTY.toMutable().setIdentifier(ExposureIdentifier.id("test")).toImmutable();

        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        listTag.add(ExposureFrame.CODEC.encode(frame, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        tag.put(randomUUID.toString(), listTag);

        ExposureFrameHistory decodedHistory = ExposureFrameHistory.load(tag, HolderLookup.Provider.create(Stream.of()));
        List<ExposureFrame> frames = decodedHistory.getFramesOf(randomUUID);

        assertEquals("test", frames.getFirst().identifier().id());
    }

    @Test
    void limit() {
        ExposureFrameHistory history = new ExposureFrameHistory(new HashMap<>());

        UUID uuid = UUID.randomUUID();

        for (int i = 0; i < ExposureFrameHistory.LIMIT + 4; i++) {
            ExposureFrame frame = ExposureFrame.EMPTY.toMutable().setIdentifier(ExposureIdentifier.id("test-" + i)).toImmutable();
            history.add(uuid, frame);
        }

        assertTrue(history.getFramesOf(uuid).size() <= ExposureFrameHistory.LIMIT);
    }
}
