package io.github.mortuusars.exposure.client.capture.method;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull Either<NativeImage, ErrorMessage> capture() {
        Either<NativeImage, ErrorMessage> fallback = fallbackMethod.capture();

        Either<NativeImage, ErrorMessage> capture = originalMethod.capture();

        if (capture.right().isPresent()) {
            onOriginalMethodFailed.accept(capture.right().get());
            return fallback;
        }

        return capture;
    }
}
