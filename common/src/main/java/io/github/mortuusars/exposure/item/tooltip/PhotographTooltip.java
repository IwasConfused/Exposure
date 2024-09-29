package io.github.mortuusars.exposure.item.tooltip;

import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public record PhotographTooltip(List<ItemAndStack<PhotographItem>> photographs) implements TooltipComponent {
}
