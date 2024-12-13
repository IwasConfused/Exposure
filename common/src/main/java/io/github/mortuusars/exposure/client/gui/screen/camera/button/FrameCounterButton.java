package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.camera.CameraClient;
import io.github.mortuusars.exposure.item.part.Attachment;
import io.github.mortuusars.exposure.item.FilmRollItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FrameCounterButton extends ImageButton {
    private final int secondaryFontColor;
    private final int mainFontColor;

    public FrameCounterButton(int x, int y, int width, int height, WidgetSprites sprites) {
        super(x, y, width, height, sprites, button -> {});
        secondaryFontColor = Config.Client.getSecondaryFontColor();
        mainFontColor = Config.Client.getMainFontColor();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {
        MutableComponent tooltipComponent = Component.translatable("gui.exposure.camera_controls.film_frame_counter.tooltip");
        if (!cameraHasFilmRoll()) {
            tooltipComponent.append(CommonComponents.NEW_LINE)
                    .append(Component.translatable("gui.exposure.camera_controls.film_frame_counter.tooltip.no_film")
                            .withStyle(Style.EMPTY.withColor(0xdd6357)));
        }

        super.renderWidget(guiGraphics, mouseX, mouseY, pPartialTick);

        String text = createText();

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int xPos = 15 + (27 - textWidth) / 2;

        guiGraphics.drawString(font, text, getX() + xPos, getY() + 8, secondaryFontColor, false);
        guiGraphics.drawString(font, text, getX() + xPos, getY() + 7, mainFontColor, false);
    }

    protected String createText() {
        return CameraClient.getActiveCamera().map(camera -> {
            ItemStack filmStack = camera.getItem().getAttachment(camera.getItemStack(), Attachment.FILM).getForReading();
            if (filmStack.isEmpty() || !(filmStack.getItem() instanceof FilmRollItem filmItem)) {
                return "-";
            }

            int exposedFrames = filmItem.getStoredFrames(filmStack).size();
            int totalFrames = filmItem.getMaxFrameCount(filmStack);
            return exposedFrames + "/" + totalFrames;
        }).orElse("-");
    }

    protected boolean cameraHasFilmRoll() {
        return CameraClient.getActiveCamera().map(camera ->
                        camera.getItem().getAttachment(camera.getItemStack(), Attachment.FILM).isEmpty())
                .orElse(false);
    }
}
