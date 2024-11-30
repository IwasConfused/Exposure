package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.image.ModifiedImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.FilmColor;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.core.image.IdentifiableImage;
import io.github.mortuusars.exposure.core.image.Image;
import io.github.mortuusars.exposure.core.pixel_modifiers.PixelModifier;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.util.GuiUtil;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NegativeExposureScreen extends ZoomableScreen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");

    public static final WidgetSprites PREV_BUTTON_SPRITES = new WidgetSprites(
            Exposure.resource("widgets/previous_button"),
            Exposure.resource("widgets/previous_button_disabled"),
            Exposure.resource("widgets/previous_button_highlighted"));

    public static final WidgetSprites NEXT_BUTTON_SPRITES = new WidgetSprites(
            Exposure.resource("widgets/next_button"),
            Exposure.resource("widgets/next_button_disabled"),
            Exposure.resource("widgets/next_button_highlighted"));

    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;

    private final Pager pager = new Pager(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get());
    private final List<ExposureIdentifier> exposures;

    public NegativeExposureScreen(List<ExposureIdentifier> exposures) {
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
                PREV_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        addRenderableWidget(previousButton);

        ImageButton nextButton = new ImageButton(width - 16, (int) (height / 2f - 16 / 2f), 16, 16,
                NEXT_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        addRenderableWidget(nextButton);

        pager.init(exposures.size(), true, previousButton, nextButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        pager.update();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        ExposureIdentifier exposureIdentifier = exposures.get(pager.getCurrentPage());

        @Nullable ExposureType type = exposureIdentifier.map(
                id -> ExposureClient.exposureCache().getOrQuery(id).map(ExposureData::getType)
                        .orElse(ExposureType.BLACK_AND_WHITE),
                texture -> (texture.getPath().endsWith("_black_and_white") || texture.getPath()
                        .endsWith("_bw")) ? ExposureType.COLOR : ExposureType.BLACK_AND_WHITE);
        if (type == null)
            type = ExposureType.BLACK_AND_WHITE;

        IdentifiableImage image = new ModifiedImage(ExposureClient.createExposureImage(exposureIdentifier), PixelModifier.NEGATIVE_FILM);

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

            FilmColor filmColor = type.getFilmColor();
            RenderSystem.setShaderColor(filmColor.r(), filmColor.g(), filmColor.b(), filmColor.a());
            GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            guiGraphics.pose().popPose();
        }

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        ExposureClient.imageRenderer().render(guiGraphics.pose(), bufferSource, image, new RenderCoordinates(width, height), type.getImageColor());

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
