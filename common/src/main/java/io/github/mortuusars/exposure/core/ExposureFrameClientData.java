package io.github.mortuusars.exposure.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ExposureFrameClientData(boolean loadingFromFile,
                                      List<UUID> capturedEntities,
                                      CompoundTag extraData) {

    public static final StreamCodec<ByteBuf, ExposureFrameClientData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ExposureFrameClientData::loadingFromFile,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list(16)), ExposureFrameClientData::capturedEntities,
            ByteBufCodecs.COMPOUND_TAG, ExposureFrameClientData::extraData,
            ExposureFrameClientData::new
    );

    public List<Entity> getCapturedEntities(ServerLevel level) {
        List<Entity> entities = new ArrayList<>();
        for (UUID uuid : this.capturedEntities) {
            @Nullable Entity entity = level.getEntity(uuid);
            if (entity != null)
                entities.add(entity);
        }
        return entities;
    }
}
