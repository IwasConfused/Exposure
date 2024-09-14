package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.LoadExposureFromFileCommandS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class LoadCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("load")
                .then(Commands.literal("with_dithering")
                        .then(Commands.argument("size", IntegerArgumentType.integer(1, 2048))
                                .then(Commands.argument("file_path", StringArgumentType.string())
                                        .then(Commands.argument("id", StringArgumentType.string())
                                                .executes(context -> loadExposureFromFile(context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        StringArgumentType.getString(context, "file_path"),
                                                        IntegerArgumentType.getInteger(context, "size"), true))))))
                .then(Commands.argument("size", IntegerArgumentType.integer(1, 2048))
                        .then(Commands.argument("file_path", StringArgumentType.string())
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .executes(context -> loadExposureFromFile(context.getSource(),
                                                StringArgumentType.getString(context, "id"),
                                                StringArgumentType.getString(context, "file_path"),
                                                IntegerArgumentType.getInteger(context, "size"), false)))));
    }

    private static int loadExposureFromFile(CommandSourceStack stack, String exposureId, String path, int size, boolean dither) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        ExposureServer.awaitExposure(exposureId, ExposureType.COLOR, player.getScoreboardName());
        Packets.sendToClient(new LoadExposureFromFileCommandS2CP(exposureId, path, size, dither), player);
        return 0;
    }
}
