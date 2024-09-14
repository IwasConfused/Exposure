package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.command.argument.TextureLocationArgument;
import io.github.mortuusars.exposure.command.suggestion.ExposureIdSuggestionProvider;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ShowExposureCommandS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ShowCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("show")
                .then(Commands.literal("latest")
                        .executes(context -> latest(context.getSource(), false))
                        .then(Commands.literal("negative")
                                .executes(context -> latest(context.getSource(), true))))
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

    private static int latest(CommandSourceStack stack, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 1;
        }

        Packets.sendToClient(ShowExposureCommandS2CP.latest(negative), player);
        return 0;
    }

    private static int exposureId(CommandSourceStack stack, String id, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 1;
        }

        ExposureData exposureData = ExposureServer.exposureStorage().get(id);
        if (exposureData.equals(ExposureData.EMPTY)) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_found", id));
            return 0;
        }

        ExposureServer.exposureSender().sendTo(id, exposureData, player);

        Packets.sendToClient(ShowExposureCommandS2CP.id(id, negative), player);

        return 0;
    }

    private static int texture(CommandSourceStack stack, ResourceLocation path, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 1;
        }

        Packets.sendToClient(ShowExposureCommandS2CP.texture(path, negative), player);

        return 0;
    }
}
