package io.github.mortuusars.exposure.integration.jade.component_provider;

import io.github.mortuusars.exposure.world.block.entity.Lightroom;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.world.lightroom.PrintingMode;
import io.github.mortuusars.exposure.integration.jade.ExposureJadePlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum LightroomComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig iPluginConfig) {
        CompoundTag tag = accessor.getServerData();

        if (tag.getBoolean("Empty"))
            return;

        IElementHelper helper = IElementHelper.get();

        tooltip.add(helper.spacer(0, 0));

        ItemStack film = ItemStack.parseOptional(accessor.getLevel().registryAccess(), tag.getCompound("Film"));
        if (!film.isEmpty()) {
            tooltip.append(helper.item(film));
            tooltip.append(helper.text(Component.literal("|").withStyle(ChatFormatting.GRAY))
                    .size(new Vec2(11, 12))
                    .translate(new Vec2(5, 6))
                    .message(null));
        }

        ItemStack paper = ItemStack.parseOptional(accessor.getLevel().registryAccess(), tag.getCompound("Paper"));
        if (!paper.isEmpty()) {
            tooltip.append(helper.item(paper));
            tooltip.append(helper.text(Component.literal("+").withStyle(ChatFormatting.GRAY))
                    .size(new Vec2(12, 12))
                    .translate(new Vec2(5, 6))
                    .message(null));
        }

        for (String dye : new String[] {"Cyan", "Yellow", "Magenta", "Black"}) {
            ItemStack stack = ItemStack.parseOptional(accessor.getLevel().registryAccess(), tag.getCompound(dye));
            if (!stack.isEmpty())
                tooltip.append(helper.item(stack));
        }

        tooltip.append(helper.progress(tag.getFloat("Progress")));

        tooltip.append(helper.item(ItemStack.parseOptional(accessor.getLevel().registryAccess(), tag.getCompound("Result"))));


        PrintingMode process = PrintingMode.fromStringOrDefault(tag.getString("Process"), PrintingMode.REGULAR);
        if (process != PrintingMode.REGULAR)
            tooltip.add(helper.text(Component.translatable("gui.exposure.lightroom.printing_mode." + process.getSerializedName())));

        tooltip.add(helper.spacer(0, 2));
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof LightroomBlockEntity lightroomBlockEntity) {
            if (lightroomBlockEntity.isEmpty()) {
                tag.putBoolean("Empty", true);
                return;
            }

            tag.put("Film", lightroomBlockEntity.getItem(Lightroom.FILM_SLOT).saveOptional(blockAccessor.getLevel().registryAccess()));
            tag.put("Paper", lightroomBlockEntity.getItem(Lightroom.PAPER_SLOT).saveOptional(blockAccessor.getLevel().registryAccess()));
            tag.put("Cyan", lightroomBlockEntity.getItem(Lightroom.CYAN_SLOT).saveOptional(blockAccessor.getLevel().registryAccess()));
            tag.put("Yellow", lightroomBlockEntity.getItem(Lightroom.YELLOW_SLOT).saveOptional(blockAccessor.getLevel().registryAccess()));
            tag.put("Magenta", lightroomBlockEntity.getItem(Lightroom.MAGENTA_SLOT).saveOptional(blockAccessor.getLevel().registryAccess()));
            tag.put("Black", lightroomBlockEntity.getItem(Lightroom.BLACK_SLOT).saveOptional(blockAccessor.getLevel().registryAccess()));
            tag.put("Result", lightroomBlockEntity.getItem(Lightroom.RESULT_SLOT).saveOptional(blockAccessor.getLevel().registryAccess()));

            tag.putString("Process", lightroomBlockEntity.getPrintingMode().getSerializedName());

            tag.putFloat("Progress", lightroomBlockEntity.getProgressPercentage());
        }
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return true;
    }

    @Override
    public ResourceLocation getUid() {
        return ExposureJadePlugin.LIGHTROOM;
    }
}
