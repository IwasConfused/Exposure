package io.github.mortuusars.exposure.client.snapshot;

import io.github.mortuusars.exposure.core.image.Image;

import java.util.concurrent.CompletableFuture;

public abstract class CaptureTask {
    private boolean started;
    private boolean done;

    public abstract CompletableFuture<TaskResult<Image>> capture();
    public abstract void frameTick();

    public boolean isStarted() {
        return started;
    }

    protected void setStarted() {
        this.started = true;
    }

    public boolean isDone() {
        return done;
    }

    protected void setDone() {
        this.done = true;
    }
}
