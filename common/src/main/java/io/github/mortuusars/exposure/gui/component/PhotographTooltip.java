package io.github.mortuusars.exposure.gui.component;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.client.render.PhotographRenderer;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotographTooltip implements ClientTooltipComponent, TooltipComponent {
    public static final int SIZE = 72;
    private final List<ItemAndStack<PhotographItem>> photographs;

    public PhotographTooltip(List<ItemAndStack<PhotographItem>> photographs) {
        this.photographs = photographs;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return SIZE;
    }

    @Override
    public int getHeight() {
        return SIZE + 2; // 2px bottom margin
    }

    @Override
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
        int photographsCount = photographs.size();
        int additionalPhotographs = Math.min(2, photographsCount - 1);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(mouseX, mouseY, 5);
        float scale = SIZE / (float) ExposureClient.exposureRenderer().getSize();
        float nextPhotographOffset = PhotographRenderer.getStackedPhotographOffset() / ExposureClient.exposureRenderer().getSize();
        scale *= 1f - (additionalPhotographs * nextPhotographOffset);
        guiGraphics.pose().scale(scale, scale, 1f);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        PhotographRenderer.renderStackedPhotographs(photographs, guiGraphics.pose(), bufferSource,
                LightTexture.FULL_BRIGHT, 255, 255, 255, 255);

        bufferSource.endBatch();

        guiGraphics.pose().popPose();

        // Stack count:
        if (photographsCount > 1) {
            guiGraphics.pose().pushPose();
            String count = Integer.toString(photographsCount);
            int fontWidth = Minecraft.getInstance().font.width(count);
            float fontScale = 1.6f;
            guiGraphics.pose().translate(
                    mouseX + ExposureClient.exposureRenderer().getSize() * scale - 2 - fontWidth * fontScale,
                    mouseY + ExposureClient.exposureRenderer().getSize() * scale - 2 - 8 * fontScale,
                    10);
            guiGraphics.pose().scale(fontScale, fontScale, fontScale);
            guiGraphics.drawString(font, count, 0, 0, 0xFFFFFFFF);
            guiGraphics.pose().popPose();
        }
    }
}
