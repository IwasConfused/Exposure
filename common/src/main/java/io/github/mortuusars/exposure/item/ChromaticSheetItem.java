package io.github.mortuusars.exposure.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.TrichromeExposureDataCreator;
import io.github.mortuusars.exposure.core.image.ExposureDataImage;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.CreateChromaticExposureS2CP;
import io.github.mortuusars.exposure.network.packet.client.WaitForExposureChangeS2CP;
import io.github.mortuusars.exposure.util.ColorChannel;
import io.github.mortuusars.exposure.warehouse.ImageData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public boolean canFinalize(ItemStack stack) {
        return getLayers(stack).size() >= 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        List<ExposureFrame> layers = getLayers(stack);

        if (!layers.isEmpty()) {
            MutableComponent component = Component.translatable("gui.exposure.channel.red")
                    .withStyle(Style.EMPTY.withColor(ColorChannel.RED.getRepresentationColor()));

            if (layers.size() >= 2) {
                component.append(Component.translatable("gui.exposure.channel.separator").withStyle(ChatFormatting.GRAY));
                component.append(Component.translatable("gui.exposure.channel.green")
                        .withStyle(Style.EMPTY.withColor(ColorChannel.GREEN.getRepresentationColor())));
            }

            if (layers.size() >= 3) {
                component.append(Component.translatable("gui.exposure.channel.separator").withStyle(ChatFormatting.GRAY));
                component.append(Component.translatable("gui.exposure.channel.blue")
                        .withStyle(Style.EMPTY.withColor(ColorChannel.BLUE.getRepresentationColor())));
            }

            tooltipComponents.add(component);

            if (layers.size() >= 3) {
                tooltipComponents.add(Component.translatable("item.exposure.chromatic_sheet.use_tooltip").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        boolean requiresFinalization = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .contains("RequiresDeferredChromaticFinalization");
        if (entity instanceof ServerPlayer player && requiresFinalization) {
            ItemStack finalizedItem = finalize(player.serverLevel(), player, stack, player.getScoreboardName());
            player.getInventory().setItem(slotId, finalizedItem);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);


        if (level instanceof ServerLevel serverLevel) {
            if (getLayers(stack).size() < 3) {
                player.displayClientMessage(Component.translatable("item.exposure.chromatic_sheet.not_enough_layers"), true);
                return InteractionResultHolder.fail(stack);
            }

            @Nullable ServerPlayer finalizingPlayer = player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
            ItemStack result = finalize(serverLevel, finalizingPlayer, stack, player.getScoreboardName());

            //TODO: TEST
//            player.setItemInHand(usedHand, result);
            return InteractionResultHolder.success(result);
        }

        return InteractionResultHolder.consume(stack);
    }

    public ItemStack finalize(ServerLevel level, @Nullable ServerPlayer player, ItemStack stack, String creator) {
        List<ExposureFrame> layers = getLayers(stack);

        Preconditions.checkState(layers.size() >= 3,
                "Finalizing Chromatic Fragment requires 3 layers. " + stack);

        ExposureFrame redFrame = layers.get(0);
        ExposureFrame greenFrame = layers.get(1);
        ExposureFrame blueFrame = layers.get(2);

        String exposureId = createChromaticExposureId(level, creator);

        ItemStack photographStack = createPhotographStack(exposureId, layers);

        boolean hasTextureLayer = layers.stream().anyMatch(data -> data.identifier().isTexture());

        if (!hasTextureLayer) {
            finalizeServerside(stack, photographStack, level, exposureId, creator, redFrame, greenFrame, blueFrame);
            return photographStack;
        } else {
            if (player != null) {
                ExposureServer.awaitExposure(exposureId, ExposureType.COLOR, player.getScoreboardName());
                Packets.sendToClient(new CreateChromaticExposureS2CP(redFrame.identifier(), greenFrame.identifier(), blueFrame.identifier(), exposureId), player);
                return photographStack;
            } else {
                CustomData.update(DataComponents.CUSTOM_DATA, stack, tag ->
                        tag.putBoolean("RequiresDeferredChromaticFinalization", true));
                return stack;
            }
        }
    }

    protected ItemStack createPhotographStack(String exposureId, List<ExposureFrame> layers) {
        ExposureFrame frameData = ExposureFrame.intersect(new ExposureIdentifier(exposureId), layers);
        frameData = frameData.toMutable()
                .setType(ExposureType.COLOR)
                .setChromatic(true)
                .toImmutable();

        ItemStack photographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
        photographStack.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frameData);
        photographStack.set(Exposure.DataComponents.PHOTOGRAPH_TYPE, ExposureType.COLOR);
        return photographStack;
    }

    protected @NotNull String createChromaticExposureId(@NotNull Level level, String idPrefix) {
        return String.format("%s_chromatic_%s", idPrefix, level.getGameTime());
    }

    protected void finalizeServerside(ItemStack chromaticSheetStack, ItemStack photographStack, ServerLevel level,
                                      String chromaticExposureId, String creator,
                                      ExposureFrame redFrame, ExposureFrame greenFrame, ExposureFrame blueFrame) {
        ExposureDataImage red = getFrameImage(redFrame);
        ExposureDataImage green = getFrameImage(greenFrame);
        ExposureDataImage blue = getFrameImage(blueFrame);

        // This tells clients not to query the exposure right away, but to wait for it
        Packets.sendToAllClients(new WaitForExposureChangeS2CP(chromaticExposureId));

        new Thread(() -> createAndSaveTrichrome(chromaticExposureId, creator, red, green, blue)).start();
    }

    protected ExposureDataImage getFrameImage(ExposureFrame frame) {
        return frame.identifier().mapId(exposureId -> {
            ExposureData exposureData = ExposureServer.exposureStorage().get(exposureId);
            return new ExposureDataImage(exposureId, exposureData);
        }).orElseGet(() -> {
            Exposure.LOGGER.error("Cannot get an image from a frame: uuid is not specified '{}'", frame);
            return new ExposureDataImage("empty", ExposureData.EMPTY);
        });
    }

    protected void createAndSaveTrichrome(String chromaticExposureId, String creator,
                                          ExposureDataImage red, ExposureDataImage green, ExposureDataImage blue) {
        try {
            ImageData trichromeImageData = TrichromeExposureDataCreator.create(red, green, blue, creator);

            ExposureData exposureData = new ExposureData(trichromeImageData.width(), trichromeImageData.height(), trichromeImageData.pixels(),
                    ExposureType.COLOR, creator, UnixTimestamp.Seconds.now(), false, new CompoundTag(), false);

            ExposureServer.exposureStorage().put(chromaticExposureId, exposureData);

            // Because we save exposure off-thread, and item was already created before chromatic processing has even begun -
            // we need to update clients, otherwise, client wouldn't know that and will think that the exposure is missing.
            ExposureServer.exposureStorage().sendExposureChanged(chromaticExposureId);
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot process and save Chromatic Photograph: {}", e.toString());
        }
    }
}
