package io.github.mortuusars.exposure.camera.viewfinder;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.camera.AttachmentType;
import io.github.mortuusars.exposure.data.filter.Filters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

public class ViewfinderShader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Minecraft minecraft = Minecraft.getInstance();
    @Nullable
    private static PostChain shader;
    private static boolean active;

    public static void apply(ResourceLocation shaderLocation) {
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

    public static void resize(int width, int height) {
        if (shader != null) {
            shader.resize(width, height);
        }
    }

    /**
     * Processes current viewfinder shader (if it is present and active).
     */
    public static void process() {
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
    public static void process(RenderTarget renderTarget) {
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
    public static void processWith(@NotNull PostChain shader, @NotNull RenderTarget renderTarget) {
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

    public static void remove() {
        if (shader != null) {
            shader.close();
        }

        shader = null;
    }

    public static void update() {
        CameraClient.getActiveCamera().ifPresentOrElse(camera -> {
            ItemStack filterStack = camera.getItem().getAttachment(camera.getItemStack(), AttachmentType.FILTER).getForReading();
            Filters.getShaderOf(filterStack).ifPresentOrElse(ViewfinderShader::apply, ViewfinderShader::remove);
        }, ViewfinderShader::remove);
    }
}
