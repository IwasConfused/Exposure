package io.github.mortuusars.exposure.util.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TimeoutTask<T> extends NestedTask<T, T> {
    private final int timeout;
    private final TimeUnit timeUnit;

    public TimeoutTask(Task<T> task, int timeout, TimeUnit timeUnit) {
        super(task);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public CompletableFuture<T> execute() {
        return getTask().execute().orTimeout(timeout, timeUnit);
    }
}
