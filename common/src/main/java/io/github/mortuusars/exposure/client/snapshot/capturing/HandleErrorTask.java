package io.github.mortuusars.exposure.client.snapshot.capturing;

import io.github.mortuusars.exposure.util.Result;
import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HandleErrorTask<T> extends Task<T> {
    private final Task<T> task;
    private final Consumer<TranslatableError> errorConsumer;

    public HandleErrorTask(Task<T> task, Consumer<TranslatableError> errorConsumer) {
        this.task = task;
        this.errorConsumer = errorConsumer;
    }

    @Override
    public CompletableFuture<T> execute() {
        return task.execute()
                .exceptionally(throwable -> {
                    errorConsumer.accept(new TranslatableError(TranslatableError.GENERIC, throwable));
                    throw new RuntimeException("Subsequent execution is stopped.");
                })
                .thenApply(executionResult -> {
                    if (executionResult instanceof Result<?> result && result.isError()) {
                        errorConsumer.accept(result.getError());
                    }

                    return executionResult;
                });
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
