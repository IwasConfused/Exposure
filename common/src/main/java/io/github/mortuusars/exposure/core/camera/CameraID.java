package io.github.mortuusars.exposure.core.camera;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record CameraID(UUID uuid) {
    public static final Codec<CameraID> CODEC = UUIDUtil.CODEC.xmap(CameraID::new, CameraID::uuid);
    public static final StreamCodec<ByteBuf, CameraID> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(CameraID::new, CameraID::uuid);

    public static CameraID createRandom() {
        return new CameraID(UUID.randomUUID());
    }
}
