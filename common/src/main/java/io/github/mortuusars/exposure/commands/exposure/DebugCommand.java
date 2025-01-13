package io.github.mortuusars.exposure.commands.exposure;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.world.camera.frame.FrameTag;
import io.github.mortuusars.exposure.world.camera.frame.Photographer;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.*;
import io.github.mortuusars.exposure.world.item.part.Attachment;
import io.github.mortuusars.exposure.world.item.part.CameraSetting;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ClearRenderingCacheS2CP;
import io.github.mortuusars.exposure.network.packet.client.StartDebugRGBCaptureS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

        Optional<Camera> camera = Optional.empty();

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem cameraItem) {
                camera = Optional.of(new CameraInHand(player, cameraItem.getOrCreateID(itemInHand), hand));
            }
        }

        List<CaptureProperties> properties = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            ColorChannel channel = ColorChannel.values()[i];
            String exposureId = ExposureIdentifier.createId(player, channel.getSerializedName());

            Holder<ColorPalette> colorPalette = ColorPalettes.get(context.getSource().registryAccess(), ColorPalettes.DEFAULT);

            properties.add(new CaptureProperties(
                    exposureId,
                    player.getUUID(),
                    camera.map(Camera::getCameraID),
                    camera.flatMap(c -> c.map(CameraSetting.SHUTTER_SPEED::getOrDefault)).orElse(ShutterSpeed.DEFAULT),
                    Optional.empty(),
                    ExposureType.BLACK_AND_WHITE,
                    camera.flatMap(c -> c.map(s -> Attachment.FILM.mapOrElse(s, FilmItem::getFrameSize, () -> 320))).orElse(320),
                    camera.flatMap(c -> c.map((cItem, cStack) -> cItem.getCropFactor())).orElse(Exposure.CROP_FACTOR),
                    colorPalette,
                    false,
                    0,
                    Optional.empty(),
                    Optional.of(channel),
                    new CompoundTag()
            ));

            Supplier<Component> msg = () -> Component.literal("Captured " + channel.getSerializedName() + " channel exposure: ")
                    .append(Component.literal(exposureId)
                            .withStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/exposure show id " + exposureId))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to view")))
                                    .withUnderlined(true)));

            ExposureServer.exposureRepository().expect(player, exposureId,
                    (pl, id) -> context.getSource().sendSuccess(msg, true));

            ExposureServer.frameHistory().add(player, new Frame(ExposureIdentifier.id(exposureId),
                    ExposureType.BLACK_AND_WHITE, new Photographer(player), Collections.emptyList(), FrameTag.EMPTY));
        }

        Packets.sendToClient(new StartDebugRGBCaptureS2CP(CaptureType.DEBUG_RGB, properties), player);

        context.getSource().sendSuccess(() -> Component.literal("Capturing RGB channels..."), true);

        return 0;
    }

    private static int chromaticFromLastThreeExposures(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        ServerPlayer player = stack.getPlayerOrException();

        List<Frame> allFrames = ExposureServer.frameHistory().getFramesOf(player)
                .stream()
                .filter(frame -> !frame.isChromatic())
                .toList();
        List<Frame> frames = new ArrayList<>(allFrames.subList(Math.max(allFrames.size() - 3, 0), allFrames.size()));

        if (frames.size() < 3) {
            stack.sendFailure(Component.literal("Not enough frames captured. 3 is required."));
            return 1;
        }

        try {
            ChromaticSheetItem item = Exposure.Items.CHROMATIC_SHEET.get();
            ItemStack itemStack = new ItemStack(item);

            for (Frame frame : frames) {
                item.addLayer(itemStack, frame);
            }

            ItemStack photographStack = item.combineIntoPhotograph(player, itemStack);
            @Nullable Frame frame = photographStack.get(Exposure.DataComponents.PHOTOGRAPH_FRAME);
            Preconditions.checkState(frame != null, "Frame data cannot be empty after combining.");

            ExposureServer.frameHistory().add(player, frame);

            Supplier<Component> msg = () -> Component.literal("Created chromatic exposure: ")
                    .append(Component.literal(frame.exposureIdentifier().toValueString())
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
                } else if (!player.addItem(developedFilmStack)) {
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
