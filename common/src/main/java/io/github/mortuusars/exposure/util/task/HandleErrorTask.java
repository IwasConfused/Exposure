package io.github.mortuusars.exposure.util.task;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.TranslatableError;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HandleErrorTask<T> extends NestedTask<T> {
    private final Consumer<TranslatableError> errorConsumer;

    public HandleErrorTask(Task<T> task, Consumer<TranslatableError> errorConsumer) {
        super(task);
        this.errorConsumer = errorConsumer;
    }

    @Override
    public CompletableFuture<T> execute() {
        return getTask().execute()
                .exceptionally(throwable -> {
                    errorConsumer.accept(new TranslatableError(TranslatableError.GENERIC, throwable));
                    Exposure.LOGGER.error("Task threw an exception: ", throwable);
                    throw new RuntimeException("Subsequent execution is stopped.");
                })
                .thenApply(executionResult -> {
                    if (executionResult instanceof Result<?> result && result.isError()) {
                        Exposure.LOGGER.error("Error: ", result.getError());
                        errorConsumer.accept(result.getError());
                    }

                    return executionResult;
                });
    }
}
