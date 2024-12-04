package io.github.mortuusars.exposure.client.snapshot.capturing;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ChainedTask<T, R> extends Task<R> {
    private final Task<T> task;
    private final Function<T, R> transformFunction;
    private final boolean async;

    public ChainedTask(Task<T> task, Function<T, R> transformFunction, boolean async) {
        this.task = task;
        this.transformFunction = transformFunction;
        this.async = async;
    }

    @Override
    public CompletableFuture<R> execute() {
        return async ? task.execute().thenApplyAsync(transformFunction) : task.execute().thenApply(transformFunction);
    }

    @Override
    public void tick() {
        task.tick();
    }

    @Override
    public boolean isStarted() {
        return task.isStarted();
    }

    @Override
    public boolean isDone() {
        return task.isDone();
    }
}
