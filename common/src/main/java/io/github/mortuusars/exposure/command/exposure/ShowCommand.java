package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.command.argument.TextureLocationArgument;
import io.github.mortuusars.exposure.command.suggestion.ExposureIdSuggestionProvider;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.warehouse.RequestedPalettedExposure;
import io.github.mortuusars.exposure.item.component.ExposureFrame;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ExposureDataResponseS2CP;
import io.github.mortuusars.exposure.network.packet.client.ShowExposureCommandS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShowCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("show")
                .then(Commands.literal("latest")
                        .executes(context -> latest(context.getSource(), context.getSource().getPlayerOrException(), false))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> latest(context.getSource(), EntityArgument.getPlayer(context, "player"), false))
                                .then(Commands.literal("negative")
                                        .executes(context -> latest(context.getSource(), EntityArgument.getPlayer(context, "player"), true))))
                        .then(Commands.literal("negative")
                                .executes(context -> latest(context.getSource(), context.getSource().getPlayerOrException(), true))))
                .then(Commands.literal("id")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests(new ExposureIdSuggestionProvider())
                                .executes(context -> exposureId(context.getSource(),
                                        StringArgumentType.getString(context, "id"), false))
                                .then(Commands.literal("negative")
                                        .executes(context -> exposureId(context.getSource(),
                                                StringArgumentType.getString(context, "id"), true)))))
                .then(Commands.literal("texture")
                        .then(Commands.argument("path", new TextureLocationArgument())
                                .executes(context -> texture(context.getSource(),
                                        ResourceLocationArgument.getId(context, "path"), false))
                                .then(Commands.literal("negative")
                                        .executes(context -> texture(context.getSource(),
                                                ResourceLocationArgument.getId(context, "path"), true)))));
    }

    private static int latest(CommandSourceStack stack, @NotNull ServerPlayer player, boolean negative) {
        List<ExposureFrame> frames = ExposureServer.frameHistory().getFramesOf(player);

        if (frames.isEmpty()) {
            stack.sendFailure(Component.literal(player.getScoreboardName() + " has not taken any photos yet."));
            return 0;
        }

        Packets.sendToClient(new ShowExposureCommandS2CP(frames, negative), player);
        return 0;
    }

    private static int exposureId(CommandSourceStack stack, String id, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 1;
        }

        ExposureIdentifier identifier = ExposureIdentifier.id(id);

        RequestedPalettedExposure palettedExposure = ExposureServer.exposureRepository().loadExposure(identifier);
        if (palettedExposure.getData().isEmpty()) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_found", id));
            return 0;
        }

        Packets.sendToClient(new ExposureDataResponseS2CP(identifier, palettedExposure), player);
        Packets.sendToClient(ShowExposureCommandS2CP.identifier(identifier, negative), player);

        return 0;
    }

    private static int texture(CommandSourceStack stack, ResourceLocation path, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 1;
        }

        ExposureIdentifier identifier = ExposureIdentifier.texture(path);
        Packets.sendToClient(ShowExposureCommandS2CP.identifier(identifier, negative), player);

        return 0;
    }
}
