package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.util.Result;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Tasks are executed in order. Second task will be returned if it is successful.
 */
public class OverrideCaptureTask<T> extends Task<Result<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Task<Result<T>> original;
    private final Task<Result<T>> override;

    public OverrideCaptureTask(Task<Result<T>> original, Task<Result<T>> override) {
        this.original = original;
        this.override = override;
    }

    @Override
    public CompletableFuture<Result<T>> execute() {
        return original.execute()
                .thenCompose(originalResult -> override.execute()
                        .handle((overrideResult, overrideException) -> {
                            if (overrideException == null && overrideResult != null && overrideResult.isSuccessful()) {
                                return overrideResult;
                            }

                            if (overrideException != null || (overrideResult != null && overrideResult.isError())) {
                                String errorMsg = overrideException != null
                                        ? overrideException.toString()
                                        : overrideResult.getError().getLocalizedMessage();
                                LOGGER.error("Override SnapShot task failed: {}", errorMsg);
                            }

                            return originalResult;
                        }));

    }

    @Override
    public void tick() {
        original.tick();
        override.tick();
    }

    @Override
    public boolean isDone() {
        return original.isDone() && override.isDone();
    }

    @Override
    public boolean isStarted() {
        return original.isStarted() || override.isStarted();
    }
}