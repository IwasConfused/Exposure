package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.util.MoreDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public class MoreDebugScreenOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (!PlatformHelper.isInDevEnv()) return;

        if (MoreDebug.isOnMoreDebugPage) {
            Minecraft.getInstance().getProfiler().push("more_debug");
            guiGraphics.drawManaged(() -> MoreDebug.render(guiGraphics));
            Minecraft.getInstance().getProfiler().pop();
            ci.cancel();
        }

        String str = "[<-] and [->] to switch pages";
        int strWidth = Minecraft.getInstance().font.width(str);
        int x = guiGraphics.guiWidth() / 2 - strWidth / 2;
        guiGraphics.fill(x - 1, 1, x + strWidth + 1, 10, -1873784752);
        guiGraphics.drawString(Minecraft.getInstance().font, str, x, 2, 0xFFFFFFFF, false);
    }
}
