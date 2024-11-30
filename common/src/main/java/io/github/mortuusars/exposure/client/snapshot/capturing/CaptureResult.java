package io.github.mortuusars.exposure.client.snapshot.capturing;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.util.ErrorMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CaptureResult {
    protected final @Nullable Image image;
    protected final @Nullable ErrorMessage errorMessage;

    private CaptureResult(@NotNull Image image) {
        this.image = image;
        this.errorMessage = null;
    }

    private CaptureResult(@NotNull ErrorMessage errorMessage) {
        this.image = null;
        this.errorMessage = errorMessage;
    }

    public static CaptureResult success(Image image) {
        return new CaptureResult(image);
    }

    public static CaptureResult success(NativeImage image) {
        return new CaptureResult(new WrappedNativeImage(image));
    }

    public static CaptureResult error(ErrorMessage errorMessage) {
        return new CaptureResult(errorMessage);
    }

    public boolean isSuccessful() {
        return image != null;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public @NotNull Image getImage() {
        Preconditions.checkState(image != null, "Called getImage on an error result. Should check with isSuccessful first.");
        return image;
    }

    public @NotNull ErrorMessage getErrorMessage() {
        Preconditions.checkState(errorMessage != null, "Called getErrorMessage on an error result. Should check with isError first.");
        return errorMessage;
    }
}
