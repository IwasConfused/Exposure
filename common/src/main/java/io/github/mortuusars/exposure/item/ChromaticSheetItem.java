package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.ExposureFrameTag;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.CreateChromaticExposureS2CP;
import io.github.mortuusars.exposure.util.ChromaChannel;
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

    public List<ExposureFrame> getLayers(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CHROMATIC_SHEET_LAYERS, Collections.emptyList());
    }

    public void addLayer(ItemStack stack, ExposureFrame frame) {
        List<ExposureFrame> layers = new ArrayList<>(getLayers(stack));
        Preconditions.checkState(layers.size() < 3, "Cannot add layer. Chromatic Sheet already has 3 layers.");
        layers.add(frame);
        stack.set(Exposure.DataComponents.CHROMATIC_SHEET_LAYERS, layers);
    }

    public boolean canCombine(ItemStack stack) {
        return getLayers(stack).size() >= 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        List<ExposureFrame> layers = getLayers(stack);

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

//    @Override
//    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
//        ItemStack stack = player.getItemInHand(usedHand);
//
//        if (level instanceof ServerLevel serverLevel) {
//            if (getLayers(stack).size() < 3) {
//                player.displayClientMessage(Component.translatable("item.exposure.chromatic_sheet.not_enough_layers"), true);
//                return InteractionResultHolder.fail(stack);
//            }
//
//            @Nullable ServerPlayer finalizingPlayer = player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
//            ItemStack result = finalize(serverLevel, finalizingPlayer, stack, player.getScoreboardName());
//
//            //TODO: TEST
////            player.setItemInHand(usedHand, result);
//            return InteractionResultHolder.success(result);
//        }
//
//        return InteractionResultHolder.consume(stack);
//    }

    public ItemStack combineIntoPhotograph(@NotNull ServerPlayer player, ItemStack stack) {
        Preconditions.checkState(canCombine(stack), "Combining Chromatic Sheet requires 3 layers. " + stack);

        ExposureIdentifier identifier = ExposureIdentifier.createId(player, "chromatic");
        List<ExposureFrame> layers = getLayers(stack);
        List<ExposureIdentifier> layersIdentifiers = layers.stream().map(ExposureFrame::identifier).toList();

        ItemStack photographStack = createPhotographStack(identifier, layers);

        ExposureServer.awaitExposure(identifier, ExposureType.COLOR, player.getScoreboardName());
        Packets.sendToClient(new CreateChromaticExposureS2CP(identifier, layersIdentifiers), player);

        return photographStack;
    }

    protected ItemStack createPhotographStack(ExposureIdentifier identifier, List<ExposureFrame> layers) {
        ExposureFrame frameData = ExposureFrame.intersect(identifier, layers);
        frameData = frameData.toMutable()
                .setType(ExposureType.COLOR)
                .updateAdditionalData(tag -> tag.putBoolean(ExposureFrameTag.CHROMATIC, true))
                .setChromatic(true)
                .toImmutable();

        ItemStack photographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
        photographStack.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frameData);
        photographStack.set(Exposure.DataComponents.PHOTOGRAPH_TYPE, ExposureType.COLOR);
        return photographStack;
    }

//    protected void finalizeServerside(ItemStack chromaticSheetStack, ItemStack photographStack, ServerLevel level,
//                                      String chromaticExposureId, String creator,
//                                      ExposureFrame redFrame, ExposureFrame greenFrame, ExposureFrame blueFrame) {
//        ExposureDataImage red = getFrameImage(redFrame);
//        ExposureDataImage green = getFrameImage(greenFrame);
//        ExposureDataImage blue = getFrameImage(blueFrame);
//
//        // This tells clients not to query the exposure right away, but to wait for it
//        Packets.sendToAllClients(new WaitForExposureChangeS2CP(chromaticExposureId));
//
//        new Thread(() -> createAndSaveTrichrome(chromaticExposureId, creator, red, green, blue)).start();
//    }

//    protected ExposureDataImage getFrameImage(ExposureFrame frame) {
//        return frame.identifier().mapId(exposureId -> {
//            ExposureData exposureData = ExposureServer.exposureStorage().get(exposureId);
//            return new ExposureDataImage(exposureId, exposureData);
//        }).orElseGet(() -> {
//            Exposure.LOGGER.error("Cannot get an image from a frame: uuid is not specified '{}'", frame);
//            return new ExposureDataImage("empty", ExposureData.EMPTY);
//        });
//    }
//
//    protected void createAndSaveTrichrome(String chromaticExposureId, String creator,
//                                          ExposureDataImage red, ExposureDataImage green, ExposureDataImage blue) {
//        try {
//            PalettedImage trichromeImageData = TrichromeCombiner.create(red, green, blue);
//
//            ExposureData exposureData = new ExposureData(trichromeImageData.width(), trichromeImageData.height(), trichromeImageData.pixels(),
//                    ExposureType.COLOR, creator, UnixTimestamp.Seconds.now(), false, new CompoundTag(), false);
//
//            ExposureServer.exposureStorage().put(chromaticExposureId, exposureData);
//
//            // Because we save exposure off-thread, and item was already created before chromatic processing has even begun -
//            // we need to update clients, otherwise, client wouldn't know that and will think that the exposure is missing.
//            ExposureServer.exposureStorage().sendExposureChanged(chromaticExposureId);
//        } catch (Exception e) {
//            Exposure.LOGGER.error("Cannot process and save Chromatic Photograph: {}", e.toString());
//        }
//    }
}
