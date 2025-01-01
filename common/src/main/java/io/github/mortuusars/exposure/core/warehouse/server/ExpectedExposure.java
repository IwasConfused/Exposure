package io.github.mortuusars.exposure.core.warehouse.server;

public record ExpectedExposure(String id, long timeoutAt, Runnable onReceived) {
    public boolean isTimedOut(long currentUnixTime) {
        return currentUnixTime > timeoutAt;
    }
}
