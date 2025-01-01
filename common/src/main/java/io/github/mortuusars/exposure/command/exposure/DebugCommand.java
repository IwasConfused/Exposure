package io.github.mortuusars.exposure.command.exposure;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.CaptureProperties;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.item.FilmRollItem;
import io.github.mortuusars.exposure.core.frame.Frame;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ClearRenderingCacheS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DebugCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("debug")
                .then(Commands.literal("clear_rendering_cache")
                        .executes(DebugCommand::clearRenderingCache))
                .then(Commands.literal("expose_rgb")
                        .executes(DebugCommand::exposeRGB))
                .then(Commands.literal("chromatic_from_last_three_exposures")
                        .executes(DebugCommand::chromaticFromLastThreeExposures))
                .then(Commands.literal("develop_film_in_hand")
                        .executes(context -> developFilmInHand(context, true))
                        .then(Commands.literal("keep_original")
                                .executes(context -> developFilmInHand(context, false))));
    }

    private static int clearRenderingCache(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(ClearRenderingCacheS2CP.INSTANCE, player);
        return 0;
    }

    private static int exposeRGB(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        ServerPlayer player = stack.getPlayerOrException();

        List<CaptureProperties> properties = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
//            properties.add(new CaptureProperties(
////                    ExposureIdentifier.createId(player),
////                    player,
//
//            ));

        }


        return 0;
    }

    private static int chromaticFromLastThreeExposures(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        ServerPlayer player = stack.getPlayerOrException();

        List<Frame> frames = ExposureServer.frameHistory().getFramesOf(player).stream()
                .filter(frame -> !frame.isChromatic()).toList();

        if (frames.size() < 3) {
            stack.sendFailure(Component.literal("Not enough frames captured. 3 is required."));
            return 1;
        }

        try {
            ChromaticSheetItem item = Exposure.Items.CHROMATIC_SHEET.get();
            ItemStack itemStack = new ItemStack(item);

            item.addLayer(itemStack, frames.get(frames.size() - 3)); // Red
            item.addLayer(itemStack, frames.get(frames.size() - 2)); // Green
            item.addLayer(itemStack, frames.getLast()); // Blue

            ItemStack photographStack = item.combineIntoPhotograph(player, itemStack);
            @Nullable Frame frame = photographStack.get(Exposure.DataComponents.PHOTOGRAPH_FRAME);
            Preconditions.checkState(frame != null, "Frame data cannot be empty after combining.");

            ExposureServer.frameHistory().add(player, frame);

            Supplier<Component> msg = () -> Component.literal("Created chromatic exposure: ")
                    .append(Component.literal(frame.exposureIdentifier().toString())
                            .withStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/exposure show id " + frame.exposureIdentifier().getId().orElse("")))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to view")))
                                    .withUnderlined(true)));

            stack.sendSuccess(msg, true);
        } catch (Exception e) {
            stack.sendFailure(Component.literal("Failed to create chromatic exposure: " + e));
            return 1;
        }

        return 0;
    }

    private static int developFilmInHand(CommandContext<CommandSourceStack> context, boolean replace) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        ServerPlayer player = stack.getPlayerOrException();

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.getItem() instanceof FilmRollItem filmRollItem) {
                DevelopedFilmItem itemType = filmRollItem.getType() == ExposureType.COLOR
                        ? Exposure.Items.DEVELOPED_COLOR_FILM.get()
                        : Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get();
                ItemStack developedFilmStack = itemInHand.transmuteCopy(itemType);

                if (replace) {
                    player.setItemInHand(hand, developedFilmStack);
                }
                else if (!player.addItem(developedFilmStack)) {
                    player.drop(developedFilmStack, true, false);
                }

                stack.sendSuccess(() -> Component.translatable("command.exposure.debug.develop.success",
                        itemInHand.getDisplayName()), true);
                return 0;
            }
        }

        stack.sendFailure(Component.translatable("command.exposure.debug.develop.fail.wrong_item"));
        return 1;
    }
}
