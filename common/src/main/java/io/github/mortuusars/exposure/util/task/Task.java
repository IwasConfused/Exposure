package io.github.mortuusars.exposure.util.task;

import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Task<T> {
    private boolean started;
    private boolean done;

    public abstract CompletableFuture<T> execute();

    public void tick() {
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isDone() {
        return done;
    }

    protected void setStarted() {
        this.started = true;
    }

    protected void setDone() {
        this.done = true;
    }

    public Task<T> onError(Consumer<TranslatableError> errorConsumer) {
        return new HandleErrorTask<>(this, errorConsumer);
    }

    public <R> Task<R> then(Function<T, R> transformFunction) {
        return new ChainedTask<>(this, transformFunction, false);
    }

    public <R> Task<R> thenAsync(Function<T, R> transformFunction) {
        return new ChainedTask<>(this, transformFunction, true);
    }

    public Task<Void> accept(Consumer<T> acceptFunction) {
        return new AcceptTask<>(this, acceptFunction, false);
    }

    public Task<Void> acceptAsync(Consumer<T> acceptFunction) {
        return new AcceptTask<>(this, acceptFunction, true);
    }
}
