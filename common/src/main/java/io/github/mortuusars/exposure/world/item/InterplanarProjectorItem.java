package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionMode;
import io.github.mortuusars.exposure.world.camera.capture.ProjectionInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class InterplanarProjectorItem extends Item {
    public InterplanarProjectorItem(Properties properties) {
        super(properties);
    }

    public ProjectionMode getMode(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.INTERPLANAR_PROJECTOR_MODE, ProjectionMode.DITHERED);
    }

    public void setMode(ItemStack stack, ProjectionMode mode) {
        stack.set(Exposure.DataComponents.INTERPLANAR_PROJECTOR_MODE, mode);
    }

    public boolean isConsumable(ItemStack stack) {
        return isAllowed();
    }

    protected boolean isAllowed() {
        return Config.Server.CAN_PROJECT_FROM_FILE.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if (!isAllowed()) {
            components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.disabled"));
        }

        components.add(getMode(stack).translate());

        if (Screen.hasShiftDown()) {
            if (isConsumable(stack)) {
                components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.consumed_info"));
            }
            components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.info"));
            components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.switch_info"));
        } else {
            components.add(Component.translatable("tooltip.exposure.hold_for_details"));
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (isAllowed() && other.isEmpty() && action == ClickAction.SECONDARY) {
            setMode(stack, getMode(stack).cycle());
            slot.setChanged();
            if (player.level().isClientSide) {
                player.playSound(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 0.6f, 1f);
            }
            return true;
        }

        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }

    public Optional<String> getFilepath(ItemStack stack) {
        @Nullable Component customName = stack.get(DataComponents.CUSTOM_NAME);
        return customName != null ? Optional.of(customName.getString()) : Optional.empty();
    }

    public Optional<ProjectionInfo> getFileLoadingData(ItemStack stack) {
        return getFilepath(stack).map(filepath -> new ProjectionInfo(filepath, getMode(stack)));
    }
}
