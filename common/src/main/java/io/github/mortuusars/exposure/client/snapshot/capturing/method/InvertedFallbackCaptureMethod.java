package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.snapshot.capturing.CaptureResult;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Fallback method is executed first, then original. Fallback would be returned in case original method failed.
 */
public class InvertedFallbackCaptureMethod implements CaptureMethod {
    protected final CaptureMethod originalMethod;
    protected final CaptureMethod fallbackMethod;
    protected final Consumer<ErrorMessage> onOriginalMethodFailed;

    public InvertedFallbackCaptureMethod(CaptureMethod originalMethod, CaptureMethod fallbackMethod, Consumer<ErrorMessage> onOriginalMethodFailed) {
        this.originalMethod = originalMethod;
        this.fallbackMethod = fallbackMethod;
        this.onOriginalMethodFailed = onOriginalMethodFailed;
    }

    @Override
    public @NotNull CompletableFuture<CaptureResult> capture() {
        return fallbackMethod.capture()
                .thenCompose(fallbackResult -> originalMethod.capture()
                        .handle((originalResult, ex) -> {
                            if (ex != null || (originalResult != null && originalResult.isError())) {
                                if (ex != null) {
                                    Exposure.LOGGER.error("Original capture method failed: {}", ex.getMessage());
                                    onOriginalMethodFailed.accept(CaptureMethod.ERROR_FAILED_GENERIC);
                                } else {
                                    onOriginalMethodFailed.accept(originalResult.getErrorMessage());
                                }

                                return fallbackResult;
                            }

                            return originalResult;
                        }));
    }

    @Override
    public void frameTick() {
        fallbackMethod.frameTick();
        originalMethod.frameTick();
    }
}
