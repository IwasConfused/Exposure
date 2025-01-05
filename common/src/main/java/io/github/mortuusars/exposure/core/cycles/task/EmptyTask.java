package io.github.mortuusars.exposure.core.cycles.task;

import java.util.concurrent.CompletableFuture;

public class EmptyTask<T> extends Task<T> {
    @Override
    public CompletableFuture<T> execute() {
        return CompletableFuture.completedFuture(null);
    }
}
