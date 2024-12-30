package io.github.mortuusars.exposure.core.warehouse.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.frame.Frame;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExposureFrameHistory extends SavedData {
    public static final Codec<ExposureFrameHistory> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.list(Frame.CODEC)).stable()
            .xmap(ExposureFrameHistory::new, ExposureFrameHistory::getFrames);

    public static final int LIMIT = 32;

    private final Map<UUID, List<Frame>> frames;

    public ExposureFrameHistory(Map<UUID, List<Frame>> frames) {
        this.frames = new HashMap<>(frames);
    }

    public Map<UUID, List<Frame>> getFrames() {
        return frames;
    }

    public List<Frame> getFramesOf(Entity entity) {
        return getFramesOf(entity.getUUID());
    }

    public List<Frame> getFramesOf(UUID uuid) {
        return frames.getOrDefault(uuid, Collections.emptyList());
    }

    public void add(Entity entity, Frame frame) {
        add(entity.getUUID(), frame);
    }

    public void add(UUID uuid, Frame frame) {
        List<Frame> list = frames.compute(uuid, (id, framesList) ->
                framesList == null ? new ArrayList<>() : new ArrayList<>(framesList));
        while (list.size() >= LIMIT) {
            list.removeFirst();
        }
        list.add(frame);
        setDirty();
    }

    public void clear() {
        frames.clear();
    }

    public void clearOf(Entity entity) {
        frames.remove(entity.getUUID());
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        DataResult<Tag> encodingResult = CODEC.encode(this, NbtOps.INSTANCE, tag);
        if (encodingResult.isSuccess()) {
            Tag encodedTag = encodingResult.getOrThrow();
            if (encodedTag instanceof CompoundTag encodedCompoundTag)
                return encodedCompoundTag;
            else {
                Exposure.LOGGER.error("Cannot save FramesHistory: '{}'. Encoded tag is not CompoundTag but a {}",
                        this, encodedTag.getType());
            }
        }
        encodingResult.error().ifPresent(error -> {
            Exposure.LOGGER.error("Cannot save FramesHistory: {}", error.message());
        });

        return tag;
    }

    public static SavedData.Factory<ExposureFrameHistory> factory() {
        return new SavedData.Factory<>(() -> new ExposureFrameHistory(new HashMap<>()), ExposureFrameHistory::load, null);
    }

    public static ExposureFrameHistory load(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        return CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
    }

    public static @NotNull ExposureFrameHistory loadOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ExposureFrameHistory.factory(), "exposure_frame_history");
    }
}
