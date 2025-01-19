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
import io.github.mortuusars.exposure.data.ColorPalette;
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
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
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

        Camera camera = CameraInHand.find(player); // Can be CameraInHand.Empty

        List<CaptureProperties> properties = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            ColorChannel channel = ColorChannel.values()[i];
            String exposureId = ExposureIdentifier.createId(player, channel.getSerializedName());

            ResourceKey<ColorPalette> paletteKey = camera.mapAttachment(Attachment.FILM, FilmItem::getColorPaletteId).orElse(ColorPalettes.DEFAULT);
            Holder<ColorPalette> colorPalette = ColorPalettes.get(context.getSource().registryAccess(), paletteKey);

            CaptureProperties captureProperties = new CaptureProperties.Builder(exposureId)
                    .setPhotographer(player)
                    .setCameraID(camera.getCameraID())
                    .setShutterSpeed(camera.getSetting(CameraSetting.SHUTTER_SPEED).orElse(null))
                    .setFilmType(ExposureType.BLACK_AND_WHITE)
                    .setFrameSize(camera.mapAttachment(Attachment.FILM, FilmItem::getFrameSize).orElse(null))
                    .setCropFactor(camera.map((cameraItem, cameraStack) -> cameraItem.getCropFactor()).orElse(null))
                    .setColorPalette(colorPalette)
                    .setChromaticChannel(channel)
                    .build();

            properties.add(captureProperties);

            Frame frame = camera
                    .map((cameraItem, cameraStack) ->
                            cameraItem.createFrame(context.getSource().getLevel(), captureProperties, player,
                                    cameraItem.getEntitiesInFrame(player, context.getSource().getLevel(), cameraStack), cameraStack))
                    .orElse(Frame.create().setIdentifier(ExposureIdentifier.id(exposureId))
                            .setType(ExposureType.BLACK_AND_WHITE)
                            .setPhotographer(new Photographer(player))
                            .toImmutable());

            Supplier<Component> msg = () -> {
                ItemStack photograph = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
                photograph.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frame);
                return Component.literal("Captured " + channel.getSerializedName() + " channel exposure: ")
                        .append(Component.literal(exposureId)
                                .withStyle(Style.EMPTY
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                "/exposure show id " + exposureId))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(photograph)))
                                        .withUnderlined(true)));
            };

            ExposureServer.exposureRepository().expect(player, exposureId, (pl, id) -> context.getSource().sendSuccess(msg, true));
            ExposureServer.frameHistory().add(player, frame);
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

            Supplier<Component> msg = () -> {
                String exposureId = frame.exposureIdentifier().getId().orElseThrow();
                ItemStack photograph = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
                photograph.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frame);
                return Component.literal("Created chromatic exposure: ")
                        .append(Component.literal(exposureId)
                                .withStyle(Style.EMPTY
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                "/exposure show id " + exposureId))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(photograph)))
                                        .withUnderlined(true)));
            };

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
