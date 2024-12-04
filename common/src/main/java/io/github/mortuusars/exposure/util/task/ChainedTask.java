package io.github.mortuusars.exposure.util.task;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ChainedTask<T, R> extends NestedTask<T, R> {
    private final Function<T, R> transformFunction;
    private final boolean async;

    public ChainedTask(Task<T> task, Function<T, R> transformFunction, boolean async) {
        super(task);
        this.transformFunction = transformFunction;
        this.async = async;
    }

    @Override
    public CompletableFuture<R> execute() {
        return async ? getTask().execute().thenApplyAsync(transformFunction) : getTask().execute().thenApply(transformFunction);
    }
}
