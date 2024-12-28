package io.github.mortuusars.exposure.core.warehouse.client;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ExposureRequestC2SP;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExposureRequester {
    public static final int TIMEOUT = 200; // 200 ticks == 10 seconds

    protected final Map<ExposureIdentifier, Long> requestedExposures = new HashMap<>();
    protected final int timeout;

    public ExposureRequester(int timeoutTicks) {
        this.timeout = timeoutTicks;
    }

    public Status request(ExposureIdentifier identifier) {
        long time = requestExposure(identifier);
        requestedExposures.put(identifier, time);
        return Status.AWAITING;
    }

    public void requestFulfilled(ExposureIdentifier identifier) {
        requestedExposures.remove(identifier);
    }

    public void refresh(ExposureIdentifier identifier) {
        requestedExposures.remove(identifier);
    }

//    public RequestStatus getStatus(ExposureIdentifier identifier) {
//        @Nullable Long requestedAt = requestedExposures.get(identifier);
//
//        if (requestedAt == null) {
//            return RequestStatus.NOT_REQUESTED;
//        }
//
//        return isTimedOut(requestedAt) ? RequestStatus.TIMED_OUT : RequestStatus.AWAITING;
//    }

    public boolean isTimedOut(ExposureIdentifier identifier) {
        @Nullable Long requestedAt = requestedExposures.get(identifier);
        return requestedAt != null && isTimedOut(requestedAt);
    }

    public int getTimeoutSeconds() {
        return timeout / 20;
    }

    private boolean isTimedOut(Long time) {
        return getGameTime() - time > TIMEOUT;
    }

    private long requestExposure(ExposureIdentifier identifier) {
        Packets.sendToServer(new ExposureRequestC2SP(identifier));
        return getGameTime();
    }

    private long getGameTime() {
        return Minecrft.level().getGameTime();
    }

    public void clear() {
        requestedExposures.clear();
    }

    public enum Status {
//        NOT_REQUESTED,
        AWAITING,
        TIMED_OUT;
    }
}
