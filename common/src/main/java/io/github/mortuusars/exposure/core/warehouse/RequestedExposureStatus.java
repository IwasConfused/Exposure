package io.github.mortuusars.exposure.core.warehouse;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum RequestedExposureStatus {
    NOT_REQUESTED,
    AWAITED,
    TIMED_OUT,
    INVALID_IDENTIFIER,
    NOT_FOUND,
    CANNOT_LOAD,
    SUCCESS,
    NEEDS_REFRESH;

    public static final StreamCodec<ByteBuf, RequestedExposureStatus> STREAM_CODEC = ByteBufCodecs.idMapper(
            id -> RequestedExposureStatus.values()[id], RequestedExposureStatus::ordinal
    );
}
