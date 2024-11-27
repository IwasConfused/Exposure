package io.github.mortuusars.exposure.client.capture.method;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.jetbrains.annotations.NotNull;

public interface CaptureMethod {
    @NotNull Either<NativeImage, ErrorMessage> capture();
}
