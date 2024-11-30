package io.github.mortuusars.exposure.client.snapshot.capturing.method;

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
        CompletableFuture<CaptureResult> fallback = fallbackMethod.capture();

        return originalMethod.capture().thenApply(result -> {
            if (result.isError()) {
                onOriginalMethodFailed.accept(result.getErrorMessage());
                return fallback.getNow(CaptureResult.error(CaptureMethod.ERROR_FAILED_GENERIC));
            }
            return result;
        });
    }
}
