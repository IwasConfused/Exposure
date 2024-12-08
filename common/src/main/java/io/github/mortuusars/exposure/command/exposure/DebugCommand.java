package io.github.mortuusars.exposure.command.exposure;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.capture.CapturedFramesHistory;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.item.FilmRollItem;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ClearRenderingCacheS2CP;
import io.github.mortuusars.exposure.network.packet.client.OnFrameAddedS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class DebugCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("debug")
                .then(Commands.literal("clear_rendering_cache")
                        .executes(DebugCommand::clearRenderingCache))
                .then(Commands.literal("chromatic_from_last_three_exposures")
                        .executes(DebugCommand::chromaticFromLastThreeExposures))
                .then(Commands.literal("develop_film_in_hand")
                        .executes(DebugCommand::developFilmInHand)
                        .then(Commands.literal("keep_original")
                                .executes(DebugCommand::developFilmInHandNoReplace)));
    }

    private static int clearRenderingCache(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(ClearRenderingCacheS2CP.INSTANCE, player);
        return 0;
    }

    private static int chromaticFromLastThreeExposures(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        ServerPlayer player = stack.getPlayerOrException();

        //TODO: per player capture history, to not mix up exposures on server.

        List<ExposureFrame> frames = CapturedFramesHistory.get();

        if (frames.size() < 3) {
            stack.sendFailure(Component.literal("Not enough frames were captured in this session. 3 is required."));
            return 1;
        }

        try {
            ChromaticSheetItem item = Exposure.Items.CHROMATIC_SHEET.get();
            ItemStack itemStack = new ItemStack(item);

            item.addLayer(itemStack, frames.get(2)); // Red
            item.addLayer(itemStack, frames.get(1)); // Green
            item.addLayer(itemStack, frames.get(0)); // Blue

            ItemStack photographStack = item.combineIntoPhotograph(player, itemStack);
            @Nullable ExposureFrame frame = photographStack.get(Exposure.DataComponents.PHOTOGRAPH_FRAME);
            Preconditions.checkState(frame != null, "Frame data cannot be empty after combining.");

            Packets.sendToClient(new OnFrameAddedS2CP(frame), player); // Adds frame to client CapturedFramesHistory

            Supplier<Component> msg = () -> Component.literal("Created chromatic exposure: ")
                    .append(Component.literal(frame.identifier().toString())
                            .withStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/exposure show id " + frame.identifier().toString()))
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

    private static int developFilmInHand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return developFilmInHand(context, true);
    }

    private static int developFilmInHandNoReplace(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return developFilmInHand(context, false);
    }
}
