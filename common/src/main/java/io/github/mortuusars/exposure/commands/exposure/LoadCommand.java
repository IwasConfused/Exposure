package io.github.mortuusars.exposure.commands.exposure;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.CaptureStartS2CP;
import io.github.mortuusars.exposure.world.camera.capture.CaptureProperties;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

import static net.minecraft.commands.Commands.*;

public class LoadCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return literal("load")
                .then(argument("capture_properties", CompoundTagArgument.compoundTag())
                        .executes(context -> load(context, CompoundTagArgument.getCompoundTag(context, "capture_properties"))));
    }

    private static int load(CommandContext<CommandSourceStack> context, CompoundTag properties) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (!properties.contains("id", CompoundTag.TAG_STRING)) {
            properties.putString("id", ExposureIdentifier.createId(player));
        }

        CaptureProperties captureProperties;

        try {
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, context.getSource().registryAccess());
            captureProperties = CaptureProperties.CODEC.decode(ops, properties).getOrThrow().getFirst();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Cannot decode properties: " + e.getMessage()));
            return 0;
        }

        if (captureProperties.projection().isEmpty()) {
            context.getSource().sendFailure(Component.literal("Cannot load: missing 'projection' property."));
            return 0;
        }

        String exposureId = captureProperties.exposureId();
        Frame frame = Frame.EMPTY.toMutable().setIdentifier(ExposureIdentifier.id(exposureId)).toImmutable();

        Supplier<Component> msg = () -> {
            ItemStack photograph = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
            photograph.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frame);

            return Component.literal("Loaded exposure: ")
                    .append(Component.literal(exposureId)
                            .withStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/exposure show id " + exposureId))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(photograph)))
                                    .withUnderlined(true)));
        };

        ExposureServer.exposureRepository().expect(player, exposureId, (pl, id) -> context.getSource().sendSuccess(msg, true));
        ExposureServer.frameHistory().add(player, frame);

        Packets.sendToClient(new CaptureStartS2CP(CaptureType.LOAD_COMMAND, captureProperties), player);

        return 0;
    }
}
