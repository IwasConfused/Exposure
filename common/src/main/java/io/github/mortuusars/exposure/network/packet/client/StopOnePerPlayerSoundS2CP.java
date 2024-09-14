package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.sound.OnePerPlayerSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record StopOnePerPlayerSoundS2CP(UUID sourcePlayerId, SoundEvent soundEvent) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("stop_one_per_player_sound");
    public static final CustomPacketPayload.Type<StopOnePerPlayerSoundS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, StopOnePerPlayerSoundS2CP> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, StopOnePerPlayerSoundS2CP::sourcePlayerId,
            SoundEvent.DIRECT_STREAM_CODEC, StopOnePerPlayerSoundS2CP::soundEvent,
            StopOnePerPlayerSoundS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (Minecraft.getInstance().level != null) {
            @Nullable Player sourcePlayer = Minecraft.getInstance().level.getPlayerByUUID(sourcePlayerId);
            if (sourcePlayer != null)
                Minecraft.getInstance().execute(() -> OnePerPlayerSounds.stop(sourcePlayer, soundEvent));
            else
                Exposure.LOGGER.info("Cannot stop OnePerPlayer sound. SourcePlayer was not found by it's UUID.");
        }

        return true;
    }
}
