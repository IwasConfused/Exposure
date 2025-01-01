package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ExposeCommandS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ExposeCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("expose")
                .executes(context -> expose(context.getSource(), Config.Server.EXPOSURE_RESOLUTION.get(), 0f))
                .then(Commands.argument("size", IntegerArgumentType.integer(1, 2048))
                        .executes(context -> expose(context.getSource(),
                                IntegerArgumentType.getInteger(context, "size"), 0f))
                        .then(Commands.argument("brightness_stops", FloatArgumentType.floatArg())
                                .executes(context -> expose(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "size"),
                                        FloatArgumentType.getFloat(context, "brightness_stops")))));
    }

    private static int expose(CommandSourceStack stack, int size, float brightnessStops) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        String exposureId = ExposureIdentifier.createId(player);
        Packets.sendToClient(new ExposeCommandS2CP(exposureId, size, brightnessStops), player);
        stack.sendSuccess(() -> Component.translatable("command.exposure.expose.started", exposureId), true);
        return 0;
    }
}
