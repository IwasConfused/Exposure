package io.github.mortuusars.exposure.core.warehouse.client;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ExposureRequestC2SP;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExposureRequester {
    public static final int TIMEOUT = 200; // 200 ticks == 10 seconds

    protected final Map<String, Long> requestedExposures = new HashMap<>();
    protected final int timeout;

    public ExposureRequester(int timeoutTicks) {
        this.timeout = timeoutTicks;
    }

    public Status request(String id) {
        long time = requestExposure(id);
        requestedExposures.put(id, time);
        return Status.AWAITING;
    }

    public void requestFulfilled(String id) {
        requestedExposures.remove(id);
    }

    public void refresh(String id) {
        requestedExposures.remove(id);
    }

//    public RequestStatus getStatus(String id) {
//        @Nullable Long requestedAt = requestedExposures.get(id);
//
//        if (requestedAt == null) {
//            return RequestStatus.NOT_REQUESTED;
//        }
//
//        return isTimedOut(requestedAt) ? RequestStatus.TIMED_OUT : RequestStatus.AWAITING;
//    }

    public boolean isTimedOut(String id) {
        @Nullable Long requestedAt = requestedExposures.get(id);
        return requestedAt != null && isTimedOut(requestedAt);
    }

    public int getTimeoutSeconds() {
        return timeout / 20;
    }

    private boolean isTimedOut(Long time) {
        return getGameTime() - time > TIMEOUT;
    }

    private long requestExposure(String id) {
        Packets.sendToServer(new ExposureRequestC2SP(id));
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
