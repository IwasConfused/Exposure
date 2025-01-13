package io.github.mortuusars.exposure.commands.exposure;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.apache.commons.lang3.NotImplementedException;

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

    //TODO: use default id if not specified
    //TODO: use CaptureData

    private static int loadExposureFromFile(CommandSourceStack stack, String exposureId, String path, int size, boolean dither) throws CommandSyntaxException {
        throw new NotImplementedException();

        //TODO: use onReceived exposure to send message

//        Component.translatable("command.exposure.load_from_file.success", id)

//        ServerPlayer player = stack.getPlayerOrException();
//        ExposureIdentifier identifier = ExposureIdentifier.id(exposureId);
//        ExposureServer.awaitExposure(identifier, ExposureType.COLOR, player.getScoreboardName());
//        Packets.sendToClient(new LoadExposureFromFileCommandS2CP(identifier, path, size, dither), player);
//        return 0;
    }
}
