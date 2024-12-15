package io.github.mortuusars.exposure.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CameraInstances extends SavedData {
    public static final Codec<CameraInstances> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.list(CameraInstance.CODEC)).stable()
            .xmap(CameraInstances::new, CameraInstances::getInstances);

    private final Map<UUID, List<CameraInstance>> instances;

    public CameraInstances(Map<UUID, List<CameraInstance>> instances) {
        this.instances = new HashMap<>(instances);
    }

    public Map<UUID, List<CameraInstance>> getInstances() {
        return instances;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        DataResult<Tag> encodingResult = CODEC.encode(this, NbtOps.INSTANCE, tag);
        if (encodingResult.isSuccess()) {
            Tag encodedTag = encodingResult.getOrThrow();
            if (encodedTag instanceof CompoundTag encodedCompoundTag)
                return encodedCompoundTag;
            else {
                Exposure.LOGGER.error("Cannot save Camera Instances: '{}'. Encoded tag is not CompoundTag but a {}",
                        this, encodedTag.getType());
            }
        }
        encodingResult.error().ifPresent(error -> {
            Exposure.LOGGER.error("Cannot save Camera Instances: {}", error.message());
        });

        return tag;
    }

    public static SavedData.Factory<CameraInstances> factory() {
        return new SavedData.Factory<>(() -> new CameraInstances(new HashMap<>()), CameraInstances::load, null);
    }

    public static CameraInstances load(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        return CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
    }

    public static @NotNull CameraInstances loadOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CameraInstances.factory(), "exposure_camera_instances");
    }
}
