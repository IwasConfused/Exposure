package io.github.mortuusars.exposure.client.gui.viewfinder;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.MC;
import io.github.mortuusars.exposure.core.camera.NewCamera;
import io.github.mortuusars.exposure.data.filter.Filters;
import io.github.mortuusars.exposure.item.part.Attachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

public class ViewfinderShader implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;
    private final Viewfinder viewfinder;
    private final NewCamera camera;

    @Nullable
    private PostChain shader;
    private boolean active;

    public ViewfinderShader(Viewfinder viewfinder, NewCamera camera) {
        this.minecraft = MC.get();
        this.viewfinder = viewfinder;
        this.camera = camera;
    }

    public void apply(ResourceLocation shaderLocation) {
        if (shader != null) {
            if (shader.getName().equals(shaderLocation.toString())) {
                return;
            }

            shader.close();
        }

        try {
            shader = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(),
                    minecraft.getMainRenderTarget(), shaderLocation);
            shader.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            active = true;
        } catch (IOException e) {
            LOGGER.warn("Failed to load shader: {}", shaderLocation, e);
            active = false;
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Failed to parse shader: {}", shaderLocation, e);
            active = false;
        }
    }

    public void resize(int width, int height) {
        if (shader != null) {
            shader.resize(width, height);
        }
    }

    /**
     * Processes current viewfinder shader (if it is present and active).
     */
    public void process() {
        if (shader != null && active) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            shader.process(minecraft.getTimer().getGameTimeDeltaTicks());
        }
    }

    /**
     * Processes current viewfinder shader (if it is present and active) to a specified render target.
     * Current shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    public void process(RenderTarget renderTarget) {
        if (shader != null && active) {
            processWith(shader, renderTarget);
        }
    }

    /**
     * Processes specified shader (if it is present and active) to a specified render target.
     * Shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    // This is probably wrong class for it, but it'll do for now.
    public void processWith(@NotNull PostChain shader, @NotNull RenderTarget renderTarget) {
        try {
            ResourceLocation shaderLocation = ResourceLocation.parse(shader.getName());

            PostChain tempShader = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(),
                    renderTarget, shaderLocation);
            tempShader.resize(renderTarget.width, renderTarget.height);

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            tempShader.process(minecraft.getTimer().getGameTimeDeltaTicks());
        } catch (IOException e) {
            LOGGER.warn("Failed to load shader: {}", shader.getName(), e);
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Failed to parse shader: {}", shader.getName(), e);
        }
    }

    public void remove() {
        if (shader != null) {
            shader.close();
        }

        shader = null;
    }

    public void update() {
        Filters.getShaderOf(Attachment.FILTER.get(camera.getItemStack()).getForReading())
                .ifPresentOrElse(this::apply, this::remove);
    }

    @Override
    public void close() {
        remove();
    }
}
