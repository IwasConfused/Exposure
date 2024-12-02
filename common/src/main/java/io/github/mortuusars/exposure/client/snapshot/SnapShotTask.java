package io.github.mortuusars.exposure.client.snapshot;

import java.util.concurrent.CompletableFuture;

public interface SnapShotTask<T> {
    CompletableFuture<TaskResult<T>> start();
    void tick();
    boolean isStarted();
    boolean isDone();
}
