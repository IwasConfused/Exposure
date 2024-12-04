package io.github.mortuusars.exposure.client.snapshot;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.snapshot.capturing.Task;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import io.github.mortuusars.exposure.util.Result;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SnapShotTask {
//    private static final Logger LOGGER = LogUtils.getLogger();
//
//    private final Task<Result<Image, ErrorMessage>> captureTask;
//    private final CompletableFuture<Image> future = new CompletableFuture<>();
//    private final List<Consumer<CompletableFuture<Image>>> resultConsumers = new ArrayList<>();
//
//    private boolean started;
//    private boolean done;
//
//    public SnapShotTask(@NotNull Task<Result<Image, ErrorMessage>> captor) {
//        this.captureTask = captor;
//    }
//
//    public boolean isStarted() {
//        return started;
//    }
//
//    public boolean isDone() {
//        return done;
//    }
//
//    public void start() {
//        started = true;
//        captureTask.execute()
//                .exceptionally(throwable -> {
//                    Exposure.LOGGER.error("Capturing failed: ", throwable);
//                    return Result.error(ErrorMessage.GENERIC);
//                })
//                .thenAccept(result -> {
//                    done = true;
//                    if (result.isSuccessful()) {
//                        future.completeAsync(result::getValue);
//                    } else {
//                        future.completeExceptionally(new RuntimeException());
//                    }
//                });
//
//        resultConsumers.forEach(consumer -> consumer.accept(future));
//    }
//
//    public void tick() {
//        if (started && !done) {
//            captureTask.tick();
//        }
//    }
//
//    public SnapShotTask consume(Consumer<CompletableFuture<Image>> resultConsumer) {
//        this.resultConsumers.add(resultConsumer
//                .andThen(future -> future.exceptionally(exception -> {
//                    LOGGER.error("Failed to handle capture result: ", exception);
//                    throw new RuntimeException();
//                })));
//        return this;
//    }
//
//    public SnapShotTask enqueue() {
//        SnapShot.enqueue(this);
//        return this;
//    }
}
