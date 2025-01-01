package io.github.mortuusars.exposure.event_hub;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mortuusars.exposure.command.ExposureCommand;
import io.github.mortuusars.exposure.command.ShaderCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommonEvents {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                        CommandBuildContext context, Commands.CommandSelection environment) {
        ExposureCommand.register(dispatcher);
        ShaderCommand.register(dispatcher);
    }
}
