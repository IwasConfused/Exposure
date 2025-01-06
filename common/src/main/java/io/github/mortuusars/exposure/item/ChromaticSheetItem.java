package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.frame.FrameTag;
import io.github.mortuusars.exposure.core.frame.Frame;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.CreateChromaticExposureS2CP;
import io.github.mortuusars.exposure.core.color.ChromaChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChromaticSheetItem extends Item {
    public ChromaticSheetItem(Properties properties) {
        super(properties);
    }

    public List<Frame> getLayers(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CHROMATIC_SHEET_LAYERS, Collections.emptyList());
    }

    public void addLayer(ItemStack stack, Frame frame) {
        List<Frame> layers = new ArrayList<>(getLayers(stack));
        Preconditions.checkState(layers.size() < 3, "Cannot add layer. Chromatic Sheet already has 3 layers.");
        layers.add(frame);
        stack.set(Exposure.DataComponents.CHROMATIC_SHEET_LAYERS, layers);
    }

    public boolean canCombine(ItemStack stack) {
        return getLayers(stack).size() >= 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        List<Frame> layers = getLayers(stack);

        if (!layers.isEmpty()) {
            MutableComponent component = Component.translatable("gui.exposure.channel.red")
                    .withStyle(Style.EMPTY.withColor(ChromaChannel.RED.getRepresentationColor()));

            if (layers.size() >= 2) {
                component.append(Component.translatable("gui.exposure.channel.separator").withStyle(ChatFormatting.GRAY));
                component.append(Component.translatable("gui.exposure.channel.green")
                        .withStyle(Style.EMPTY.withColor(ChromaChannel.GREEN.getRepresentationColor())));
            }

            if (layers.size() >= 3) {
                component.append(Component.translatable("gui.exposure.channel.separator").withStyle(ChatFormatting.GRAY));
                component.append(Component.translatable("gui.exposure.channel.blue")
                        .withStyle(Style.EMPTY.withColor(ChromaChannel.BLUE.getRepresentationColor())));
            }

            tooltipComponents.add(component);

            if (layers.size() >= 3) {
                tooltipComponents.add(Component.translatable("item.exposure.chromatic_sheet.use_tooltip").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof ServerPlayer player && canCombine(stack)) {
            ItemStack finalizedItem = combineIntoPhotograph(player, stack);
            player.getInventory().setItem(slotId, finalizedItem);
        }
    }

    public ItemStack combineIntoPhotograph(@NotNull ServerPlayer player, ItemStack stack) {
        Preconditions.checkState(canCombine(stack), "Combining Chromatic Sheet requires 3 layers. " + stack);

        String exposureId = ExposureIdentifier.createId(player, "chromatic");
        List<Frame> layers = getLayers(stack);
        List<ExposureIdentifier> layersIdentifiers = layers.stream().map(Frame::exposureIdentifier).toList();

        ItemStack photographStack = createPhotographStack(ExposureIdentifier.id(exposureId), layers);

        ExposureServer.exposureRepository().expect(player, exposureId);
        Packets.sendToClient(new CreateChromaticExposureS2CP(exposureId, layersIdentifiers), player);

        return photographStack;
    }

    protected ItemStack createPhotographStack(ExposureIdentifier identifier, List<Frame> layers) {
        Frame frameData = Frame.intersect(identifier, layers);
        frameData = frameData.toMutable()
                .setType(ExposureType.COLOR)
                .updateTag(tag -> tag.putBoolean(FrameTag.CHROMATIC, true))
                .setChromatic(true)
                .toImmutable();

        ItemStack photographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
        photographStack.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frameData);
        photographStack.set(Exposure.DataComponents.PHOTOGRAPH_TYPE, ExposureType.COLOR);
        return photographStack;
    }
}
