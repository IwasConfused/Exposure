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

public record ExposureFrameDataFromClient(boolean loadingFromFile,
                                          List<UUID> entitiesInFrameIds,
                                          CompoundTag extraData) {

    public static final StreamCodec<ByteBuf, ExposureFrameDataFromClient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ExposureFrameDataFromClient::loadingFromFile,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list(16)), ExposureFrameDataFromClient::entitiesInFrameIds,
            ByteBufCodecs.COMPOUND_TAG, ExposureFrameDataFromClient::extraData,
            ExposureFrameDataFromClient::new
    );

    public ExposureFrameDataFromClient(boolean loadingFromFile,
                                       List<UUID> entitiesInFrame) {
        this(loadingFromFile, entitiesInFrame, new CompoundTag());
    }

    public List<Entity> getEntitiesInFrame(ServerLevel level) {
        List<Entity> entitiesInFrame = new ArrayList<>();
        for (UUID uuid : entitiesInFrameIds) {
            @Nullable Entity entity = level.getEntity(uuid);
            if (entity != null)
                entitiesInFrame.add(entity);
        }
        return entitiesInFrame;
    }
}
