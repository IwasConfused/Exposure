package io.github.mortuusars.exposure.foundation.warehouse;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.foundation.warehouse.client.ExposureRequester;
import io.github.mortuusars.exposure.foundation.warehouse.client.RequestedExposureStatus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public class RequestedExposureData {
    public static final RequestedExposureData NOT_REQUESTED = status(RequestedExposureStatus.NOT_REQUESTED);
    public static final RequestedExposureData AWAITING = status(RequestedExposureStatus.AWAITED);
    public static final RequestedExposureData TIMED_OUT = status(RequestedExposureStatus.TIMED_OUT);

    public static final RequestedExposureData INVALID_IDENTIFIER = status(RequestedExposureStatus.INVALID_IDENTIFIER);
    public static final RequestedExposureData NOT_FOUND = status(RequestedExposureStatus.NOT_FOUND);
    public static final RequestedExposureData CANNOT_LOAD = status(RequestedExposureStatus.CANNOT_LOAD);

    public static final StreamCodec<FriendlyByteBuf, RequestedExposureData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ExposureData.STREAM_CODEC), RequestedExposureData::getData,
            RequestedExposureStatus.STREAM_CODEC, RequestedExposureData::getStatus,
            RequestedExposureData::fromOptional
    );

    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    protected final ExposureData data;
    protected final RequestedExposureStatus status;

    protected RequestedExposureData(@Nullable ExposureData data, RequestedExposureStatus status) {
        this.data = data;
        this.status = status;
    }

    private static RequestedExposureData fromOptional(Optional<ExposureData> data, RequestedExposureStatus status) {
        return new RequestedExposureData(data.orElse(null), status);
    }

    private static RequestedExposureData status(RequestedExposureStatus status) {
        Preconditions.checkArgument(status != RequestedExposureStatus.SUCCESS && status != RequestedExposureStatus.NEEDS_REFRESH,
                "Successful result cannot be created without data.");
        return new RequestedExposureData(null, status);
    }

    public static RequestedExposureData success(ExposureData data) {
        Preconditions.checkNotNull(data, "Successful result cannot be created without data.");
        if (data.equals(ExposureData.EMPTY)) {
            LOGGER.warn("ExposureData.EMPTY is used to create successful ExposureResult. This is probably not intentional.");
        }
        return new RequestedExposureData(data, RequestedExposureStatus.SUCCESS);
    }

    public static RequestedExposureData needsRefresh(RequestedExposureData result) {
        Preconditions.checkArgument(result.is(RequestedExposureStatus.SUCCESS));
        return new RequestedExposureData(result.data, RequestedExposureStatus.NEEDS_REFRESH);
    }

    public static RequestedExposureData fromRequestStatus(ExposureRequester.Status status) {
        return switch (status) {
//            case NOT_REQUESTED -> NOT_REQUESTED;
            case AWAITING -> AWAITING;
            case TIMED_OUT -> TIMED_OUT;
            case null, default -> throw new IllegalArgumentException(status + " is unexpected.");
        };
    }

    public Optional<ExposureData> getData() {
        return Optional.ofNullable(data);
    }

    public ExposureData orElse(ExposureData orElse) {
        return data != null ? data : orElse;
    }

    public RequestedExposureStatus getStatus() {
        return status;
    }

    public boolean is(RequestedExposureStatus status) {
        return this.status.equals(status);
    }
}
