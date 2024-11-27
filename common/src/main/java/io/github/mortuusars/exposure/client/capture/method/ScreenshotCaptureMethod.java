package io.github.mortuusars.exposure.client.capture.method;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.util.ErrorMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.NotNull;

public class ScreenshotCaptureMethod implements CaptureMethod {
    @Override
    public @NotNull Either<NativeImage, ErrorMessage> capture() {
        return Either.left(Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget()));
    }
}
