package io.github.mortuusars.exposure.client.gui;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Widgets {
    public static final WidgetSprites PREVIOUS_BUTTON_SPRITES =
            threeStateSprites(Exposure.resource("widgets/previous_button"));
    public static final WidgetSprites NEXT_BUTTON_SPRITES =
            threeStateSprites(Exposure.resource("widgets/next_button"));
    public static final WidgetSprites CONFIRM_BUTTON_SPRITES =
            threeStateSprites(Exposure.resource("widgets/confirm_button"));
    public static final WidgetSprites CANCEL_BUTTON_SPRITES =
            threeStateSprites(Exposure.resource("widgets/cancel_button"));

    public static WidgetSprites threeStateSprites(ResourceLocation base) {
        return new WidgetSprites(base,
                ResourceLocation.fromNamespaceAndPath(base.getNamespace(), base.getPath() + "_disabled"),
                ResourceLocation.fromNamespaceAndPath(base.getNamespace(), base.getPath() + "_highlighted"));
    }

    public static <T> Map<T, WidgetSprites> createMap(List<T> values, Function<T, WidgetSprites> convertFunc) {
        Preconditions.checkArgument(!values.isEmpty(), "values list must not be empty.");
        Map<T, WidgetSprites> map = new HashMap<>();
        for (T value : values) {
            map.put(value, convertFunc.apply(value));
        }
        return map;
    }
}
