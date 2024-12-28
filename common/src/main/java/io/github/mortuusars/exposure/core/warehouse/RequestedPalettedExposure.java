package io.github.mortuusars.exposure.core.warehouse;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.warehouse.client.ExposureRequester;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public class RequestedPalettedExposure {
    public static final RequestedPalettedExposure NOT_REQUESTED = status(RequestedExposureStatus.NOT_REQUESTED);
    public static final RequestedPalettedExposure AWAITING = status(RequestedExposureStatus.AWAITED);
    public static final RequestedPalettedExposure TIMED_OUT = status(RequestedExposureStatus.TIMED_OUT);

    public static final RequestedPalettedExposure INVALID_ID = status(RequestedExposureStatus.INVALID_ID);
    public static final RequestedPalettedExposure NOT_FOUND = status(RequestedExposureStatus.NOT_FOUND);
    public static final RequestedPalettedExposure CANNOT_LOAD = status(RequestedExposureStatus.CANNOT_LOAD);

    public static final StreamCodec<FriendlyByteBuf, RequestedPalettedExposure> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(PalettedExposure.STREAM_CODEC), RequestedPalettedExposure::getData,
            RequestedExposureStatus.STREAM_CODEC, RequestedPalettedExposure::getStatus,
            RequestedPalettedExposure::fromOptional
    );

    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    protected final PalettedExposure data;
    protected final RequestedExposureStatus status;

    protected RequestedPalettedExposure(@Nullable PalettedExposure data, RequestedExposureStatus status) {
        this.data = data;
        this.status = status;
    }

    private static RequestedPalettedExposure fromOptional(Optional<PalettedExposure> data, RequestedExposureStatus status) {
        return new RequestedPalettedExposure(data.orElse(null), status);
    }

    private static RequestedPalettedExposure status(RequestedExposureStatus status) {
        Preconditions.checkArgument(status != RequestedExposureStatus.SUCCESS && status != RequestedExposureStatus.NEEDS_REFRESH,
                "Successful result cannot be created without data.");
        return new RequestedPalettedExposure(null, status);
    }

    public static RequestedPalettedExposure success(PalettedExposure data) {
        Preconditions.checkNotNull(data, "Successful result cannot be created without data.");
        if (data.equals(PalettedExposure.EMPTY)) {
            LOGGER.warn("ExposureData.EMPTY is used to create successful ExposureResult. This is probably not intentional.");
        }
        return new RequestedPalettedExposure(data, RequestedExposureStatus.SUCCESS);
    }

    public static RequestedPalettedExposure needsRefresh(RequestedPalettedExposure result) {
        Preconditions.checkArgument(result.is(RequestedExposureStatus.SUCCESS));
        return new RequestedPalettedExposure(result.data, RequestedExposureStatus.NEEDS_REFRESH);
    }

    public static RequestedPalettedExposure fromRequestStatus(ExposureRequester.Status status) {
        return switch (status) {
//            case NOT_REQUESTED -> NOT_REQUESTED;
            case AWAITING -> AWAITING;
            case TIMED_OUT -> TIMED_OUT;
            case null, default -> throw new IllegalArgumentException(status + " is unexpected.");
        };
    }

    public Optional<PalettedExposure> getData() {
        return Optional.ofNullable(data);
    }

    public PalettedExposure orElse(PalettedExposure orElse) {
        return data != null ? data : orElse;
    }

    public RequestedExposureStatus getStatus() {
        return status;
    }

    public boolean is(RequestedExposureStatus status) {
        return this.status.equals(status);
    }
}
