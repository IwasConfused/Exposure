package io.github.mortuusars.exposure.camera.viewfinder;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.core.camera.AttachmentType;
import io.github.mortuusars.exposure.data.filter.Filters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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

    public static void process() {
        if (shader != null && active) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            shader.process(minecraft.getTimer().getGameTimeDeltaTicks());
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
