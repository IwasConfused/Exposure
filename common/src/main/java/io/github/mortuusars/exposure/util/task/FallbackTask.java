package io.github.mortuusars.exposure.util.task;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.util.TranslatableError;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * If first task is failed, second task will be executed, and it's result returned.
 */
public class FallbackTask<T> extends Task<Result<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Task<Result<T>> main;
    private final Task<Result<T>> fallback;

    public FallbackTask(Task<Result<T>> main, Task<Result<T>> fallback) {
        this.main = main;
        this.fallback = fallback;
    }

    @Override
    public CompletableFuture<Result<T>> execute() {
        return main.execute()
                .handle((mainResult, mainException) -> {
                    if (mainResult != null && mainResult.isSuccessful()) {
                        return mainResult;
                    } else if (mainException != null) {
                        LOGGER.error("Main CaptureTask is failed: ", mainException);
                    } else if (mainResult != null && mainResult.isError()) {
                        LOGGER.error("Main CaptureTask is failed: {}", mainResult.getError().getLocalizedMessage());
                    }

                    return captureFallback();
                });
    }

    private Result<T> captureFallback() {
        return fallback.execute().handle((fallbackResult, fallbackException) -> {
            if (fallbackException != null) {
                LOGGER.error("Both Main and Fallback tasks are failed!");
                return Result.<T>error(new TranslatableError(TranslatableError.GENERIC, fallbackException));
            }
            return fallbackResult;
        }).join();
    }

    @Override
    public void tick() {
        main.tick();
        fallback.tick();
    }

    @Override
    public boolean isDone() {
        return main.isDone() && fallback.isDone();
    }

    @Override
    public boolean isStarted() {
        return main.isStarted() || fallback.isStarted();
    }
}