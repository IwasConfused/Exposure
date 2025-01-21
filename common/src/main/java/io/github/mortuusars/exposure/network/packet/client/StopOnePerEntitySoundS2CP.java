package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.client.sound.OnePerEntitySoundsClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record StopOnePerEntitySoundS2CP(int entityId, SoundEvent soundEvent) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("stop_one_per_entity_sound");
    public static final CustomPacketPayload.Type<StopOnePerEntitySoundS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, StopOnePerEntitySoundS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StopOnePerEntitySoundS2CP::entityId,
            SoundEvent.DIRECT_STREAM_CODEC, StopOnePerEntitySoundS2CP::soundEvent,
            StopOnePerEntitySoundS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (entity != null) {
            OnePerEntitySoundsClient.stop(entity, soundEvent);
        }
        return true;
    }
}
