package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.gui.ClientGUI;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.menu.ItemRenameMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilmRollItem extends Item implements IFilmItem {
    private final ExposureType exposureType;
    private final int barColor;

    public FilmRollItem(ExposureType exposureType, int barColor, Properties properties) {
        super(properties);
        this.exposureType = exposureType;
        this.barColor = barColor;
    }

    @Override
    public ExposureType getType() {
        return exposureType;
    }

    public boolean isBarVisible(@NotNull ItemStack stack) {
        return hasFrames(stack);
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.min(1 + 12 * getStoredFramesCount(stack) / getMaxFrameCount(stack), 13);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return barColor;
    }

    public void addFrame(ItemStack stack, ExposureFrame frame) {
        Preconditions.checkState(getStoredFramesCount(stack) < getMaxFrameCount(stack),
                "Cannot add more frames than film could fit. Size: " + getMaxFrameCount(stack));

        List<ExposureFrame> frames = new ArrayList<>(stack.getOrDefault(Exposure.DataComponents.FILM_FRAMES, Collections.emptyList()));
        frames.add(frame);

        stack.set(Exposure.DataComponents.FILM_FRAMES, frames);
    }

    public boolean canAddFrame(ItemStack stack) {
        return getStoredFramesCount(stack) < getMaxFrameCount(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int exposedFrames = getStoredFramesCount(stack);
        if (exposedFrames > 0) {
            int totalFrames = getMaxFrameCount(stack);
            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.frame_count", exposedFrames, totalFrames)
                    .withStyle(ChatFormatting.GRAY));
        }

        int frameSize = getFrameSize(stack);
        if (frameSize != getDefaultFrameSize(stack)) {
            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.frame_size",
                    Component.literal(String.format("%.1f", frameSize / 10f)))
                            .withStyle(ChatFormatting.GRAY));
        }

        if (Config.Common.FILM_ROLL_RENAMING.get()) {
            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.renaming"));
        }

//        // Create compat:
//        int developingStep = stack.getTag() != null ? stack.getTag().getInt("CurrentDevelopingStep") : 0;
//        if (Config.Common.CREATE_SPOUT_DEVELOPING_ENABLED.get() && developingStep > 0) {
//            List<? extends String> totalSteps = Config.Common.spoutDevelopingSequence(getType()).get();
//
//            MutableComponent stepsComponent = Component.literal("");
//
//            for (int i = 0; i < developingStep; i++) {
//                stepsComponent.append(Component.literal("I").withStyle(ChatFormatting.GOLD));
//            }
//
//            for (int i = developingStep; i < totalSteps.size(); i++) {
//                stepsComponent.append(Component.literal("I").withStyle(ChatFormatting.DARK_GRAY));
//            }
//
//            tooltipComponents.add(Component.translatable("item.exposure.film_roll.tooltip.developing_step", stepsComponent)
//                    .withStyle(ChatFormatting.GOLD));
//        }

        //noinspection ConstantValue
        if (exposedFrames > 0 && !PlatformHelper.isModLoaded("jei") && Config.Client.RECIPE_TOOLTIPS_WITHOUT_JEI.get()) {
            ClientGUI.addFilmRollDevelopingTooltip(stack, context, tooltipComponents, tooltipFlag);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!Config.Common.FILM_ROLL_RENAMING.get() || !(player instanceof ServerPlayer serverPlayer)) {
            return super.use(level, player, usedHand);
        }

        int slot = getMatchingSlotInInventory(player.getInventory(), player.getItemInHand(usedHand));
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.translatable("gui.exposure.item_rename.title");
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return new ItemRenameMenu(containerId, playerInventory, slot);
            }
        };
        PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> buffer.writeInt(slot));
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    protected int getMatchingSlotInInventory(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).equals(stack)) {
                return i;
            }
        }
        return -1;
    }
}
