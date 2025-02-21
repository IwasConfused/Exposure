package io.github.mortuusars.exposure.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.gui.screen.element.Pager;
import io.github.mortuusars.exposure.render.image.RenderedImageProvider;
import io.github.mortuusars.exposure.render.image.ExposureDataImage;
import io.github.mortuusars.exposure.render.image.IImage;
import io.github.mortuusars.exposure.render.image.TextureImage;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import io.github.mortuusars.exposure.util.GuiUtil;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NegativeExposureScreen extends ZoomableScreen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;

    private final Pager pager = new Pager(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get());
    private final List<Either<String, ResourceLocation>> exposures;

    public NegativeExposureScreen(List<Either<String, ResourceLocation>> exposures) {
        super(Component.empty());
        this.exposures = exposures;
        Preconditions.checkArgument(exposures != null && !exposures.isEmpty());

        zoom.step = 2f;
        zoom.defaultZoom = 1f;
        zoom.targetZoom = 1f;
        zoom.minZoom = zoom.defaultZoom / (float)Math.pow(zoom.step, 1f);
        zoom.maxZoom = zoom.defaultZoom * (float)Math.pow(zoom.step, 5f);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        zoomFactor = 1f / (minecraft.options.guiScale().get() + 1);
        ImageButton previousButton = new ImageButton(0, (int) (height / 2f - 16 / 2f), 16, 16,
                0, 0, 16, PhotographScreen.WIDGETS_TEXTURE, 256, 256,
                button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        addRenderableWidget(previousButton);

        ImageButton nextButton = new ImageButton(width - 16, (int) (height / 2f - 16 / 2f), 16, 16,
                16, 0, 16, PhotographScreen.WIDGETS_TEXTURE, 256, 256,
                button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        addRenderableWidget(nextButton);

        pager.init(exposures.size(), true, previousButton, nextButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        pager.update();

        renderBackground(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Either<String, ResourceLocation> idOrTexture = exposures.get(pager.getCurrentPage());

        @Nullable FilmType type = idOrTexture.map(
                id -> ExposureClient.getExposureStorage().getOrQuery(id).map(ExposureSavedData::getType)
                        .orElse(FilmType.BLACK_AND_WHITE),
                texture -> (texture.getPath().endsWith("_black_and_white") || texture.getPath()
                        .endsWith("_bw")) ? FilmType.COLOR : FilmType.BLACK_AND_WHITE);
        if (type == null)
            type = FilmType.BLACK_AND_WHITE;

        @Nullable IImage image = idOrTexture.map(
                id -> ExposureClient.getExposureStorage().getOrQuery(id)
                        .map(data -> new ExposureDataImage(id, data)).orElse(null),
                TextureImage::getTexture
        );

        if (image == null)
            return;

        int width = image.getWidth();
        int height = image.getHeight();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + this.width / 2f, y + this.height / 2f, 0);
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.pose().translate(-width / 2f, -height / 2f, 0);

        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, TEXTURE);

            guiGraphics.pose().pushPose();
            float scale = Math.max((float) width / (FRAME_SIZE), (float) height / (FRAME_SIZE));
            guiGraphics.pose().scale(scale, scale, scale);
            guiGraphics.pose().translate(-12, -12, 0);

            GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);

            RenderSystem.setShaderColor(type.filmR, type.filmG, type.filmB, type.filmA);
            GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            guiGraphics.pose().popPose();
        }

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        ExposureClient.getExposureRenderer().render(new RenderedImageProvider(image), ExposurePixelModifiers.NEGATIVE_FILM,
                guiGraphics.pose(), bufferSource, 0, 0, width, height, 0, 0, 1, 1,
                LightTexture.FULL_BRIGHT, type.frameR, type.frameG, type.frameB, 255);
        bufferSource.endBatch();

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return pager.handleKeyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return pager.handleKeyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }
}
