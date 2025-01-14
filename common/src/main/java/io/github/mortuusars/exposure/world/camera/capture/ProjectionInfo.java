package io.github.mortuusars.exposure.world.camera.capture;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ProjectionInfo(String path, ProjectionMode mode) {
    public static final StreamCodec<FriendlyByteBuf, ProjectionInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ProjectionInfo::path,
            ProjectionMode.STREAM_CODEC, ProjectionInfo::mode,
            ProjectionInfo::new
    );
}
