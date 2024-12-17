package io.github.mortuusars.exposure.client.snapshot.capturing.method;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.gui.viewfinder.Viewfinders;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.client.snapshot.capturing.Capture;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.task.Result;
import io.github.mortuusars.exposure.util.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.PostChain;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Captures a screenshot without showing it on screen. Makes photographing a seamless experience™.
 */
public class BackgroundScreenshotCaptureTask extends Task<Result<Image>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public CompletableFuture<Result<Image>> execute() {
        Minecraft minecraft = Minecraft.getInstance();

        RenderTarget renderTarget = new TextureTarget(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight(), true, Minecraft.ON_OSX);
        renderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        renderTarget.clear(Minecraft.ON_OSX);

        try {
            // For whatever reason setPanoramicMode makes water visible again. So we cannot omit it.
            minecraft.gameRenderer.setPanoramicMode(true);

            minecraft.gameRenderer.setRenderBlockOutline(false);

            minecraft.levelRenderer.graphicsChanged();
            renderTarget.bindWrite(false);
            minecraft.gameRenderer.renderLevel(minecraft.getTimer());

            applyShaderEffects(renderTarget);

            if (ExposureClient.isIrisOrOculusInstalled()) {
                LOGGER.warn("BackgroundScreenshotCaptureMethod is used while Iris or Oculus is installed. " +
                        "Captured image most likely will not look as expected.");
            }

            WrappedNativeImage image = new WrappedNativeImage(Screenshot.takeScreenshot(renderTarget));

            return CompletableFuture.completedFuture(Result.success(image));
        } catch (Exception e) {
            LOGGER.error("Couldn't capture image: ", e);
            return CompletableFuture.completedFuture(Result.error(Capture.ERROR_FAILED_GENERIC));
        } finally {
            minecraft.gameRenderer.setPanoramicMode(false);
            minecraft.gameRenderer.setRenderBlockOutline(true);
            renderTarget.destroyBuffers();
            minecraft.levelRenderer.graphicsChanged();
            minecraft.getMainRenderTarget().bindWrite(true);
        }
    }

    private static void applyShaderEffects(RenderTarget renderTarget) {
        Viewfinders.getActive().flatMap(Viewfinder::getShader).ifPresent(shader -> {
            @Nullable PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
            if (effect != null && Minecraft.getInstance().gameRenderer.effectActive) {
                shader.processWith(effect, renderTarget);
            }

            shader.process(renderTarget);
        });

//        @Nullable PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
//        if (effect != null && Minecraft.getInstance().gameRenderer.effectActive) {
//
//            OldViewfinderShader.processWith(effect, renderTarget);
//        }
//
//        OldViewfinderShader.process(renderTarget);
    }
}
