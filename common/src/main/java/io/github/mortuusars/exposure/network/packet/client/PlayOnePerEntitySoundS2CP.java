package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.sound.OnePerEntitySoundsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PlayOnePerEntitySoundS2CP(UUID sourceEntityID, SoundEvent soundEvent, SoundSource source,
                                        float volume, float pitch) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("play_one_per_entity_sound");
    public static final CustomPacketPayload.Type<PlayOnePerEntitySoundS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayOnePerEntitySoundS2CP> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayOnePerEntitySoundS2CP::sourceEntityID,
            SoundEvent.DIRECT_STREAM_CODEC, PlayOnePerEntitySoundS2CP::soundEvent,
            ByteBufCodecs.idMapper(i -> SoundSource.values()[i], SoundSource::ordinal), PlayOnePerEntitySoundS2CP::source,
            ByteBufCodecs.FLOAT, PlayOnePerEntitySoundS2CP::volume,
            ByteBufCodecs.FLOAT, PlayOnePerEntitySoundS2CP::pitch,
            PlayOnePerEntitySoundS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (Minecraft.getInstance().level != null) {
            @Nullable Entity sourceEntity = Minecraft.getInstance().level.getEntities().get(sourceEntityID);
            if (sourceEntity != null) {
                Minecraft.getInstance().execute(() -> OnePerEntitySoundsClient.play(sourceEntity, soundEvent, source, volume, pitch));
            } else {
                Exposure.LOGGER.debug("Cannot play OnePerEntity sound. Source Entity was not found by it's UUID.");
            }
        }

        return true;
    }
}
