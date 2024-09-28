package io.github.mortuusars.exposure.client.gui;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public class Widgets {
    public static final WidgetSprites PREVIOUS_BUTTON_SPRITES =
            regularDisabledHighlightedSprites(Exposure.resource("widgets/previous_button"));
    public static final WidgetSprites NEXT_BUTTON_SPRITES =
            regularDisabledHighlightedSprites(Exposure.resource("widgets/next_button"));
    public static final WidgetSprites CONFIRM_BUTTON_SPRITES =
            regularDisabledHighlightedSprites(Exposure.resource("widgets/confirm_button"));
    public static final WidgetSprites CANCEL_BUTTON_SPRITES =
            regularDisabledHighlightedSprites(Exposure.resource("widgets/cancel_button"));

    public static WidgetSprites regularDisabledHighlightedSprites(ResourceLocation base) {
        return new WidgetSprites(base,
                ResourceLocation.fromNamespaceAndPath(base.getNamespace(), base.getPath() + "_disabled"),
                ResourceLocation.fromNamespaceAndPath(base.getNamespace(), base.getPath() + "_highlighted"));
    }
}
