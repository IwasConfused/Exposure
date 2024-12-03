package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.snapshot.TaskResult;
import io.github.mortuusars.exposure.core.image.Image;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Tasks are executed in order. Second task will be returned if it is successful.
 */
public class OverrideCaptureTask extends CaptureTask {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CaptureTask original;
    private final CaptureTask override;

    public OverrideCaptureTask(CaptureTask original, CaptureTask override) {
        this.original = original;
        this.override = override;
    }

    @Override
    public CompletableFuture<TaskResult<Image>> capture() {
        return original.capture()
                .thenCompose(originalResult -> override.capture()
                        .handle((overrideResult, overrideException) -> {
                            if (overrideException == null && overrideResult != null && overrideResult.isSuccessful()) {
                                return overrideResult;
                            }

                            if (overrideException != null || (overrideResult != null && overrideResult.isError())) {
                                String errorMsg = overrideException != null
                                        ? overrideException.toString()
                                        : overrideResult.getErrorMessage().getTechnicalTranslation().getString();
                                LOGGER.error("Override SnapShot task failed: {}", errorMsg);
                            }

                            return originalResult;
                        }));

    }

    @Override
    public void frameTick() {
        original.frameTick();
        override.frameTick();
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