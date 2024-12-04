package io.github.mortuusars.exposure.client.snapshot.capturing;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AcceptTask<T> extends Task<Void> {
    private final Task<T> task;
    private final Consumer<T> acceptor;
    private final boolean async;

    public AcceptTask(Task<T> task, Consumer<T> acceptor, boolean async) {
        this.task = task;
        this.acceptor = acceptor;
        this.async = async;
    }

    @Override
    public CompletableFuture<Void> execute() {
        return async ? task.execute().thenAcceptAsync(acceptor) : task.execute().thenAccept(acceptor);
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
