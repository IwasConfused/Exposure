package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.client.sound.OnePerEntitySoundsClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record PlayOnePerEntityShutterTickingSoundS2CP(int entityId,
                                                      CameraID cameraID,
                                                      float volume,
                                                      float pitch,
                                                      int durationTicks) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("play_one_per_entity_shutter_ticking_sound");
    public static final Type<PlayOnePerEntityShutterTickingSoundS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayOnePerEntityShutterTickingSoundS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayOnePerEntityShutterTickingSoundS2CP::entityId,
            CameraID.STREAM_CODEC, PlayOnePerEntityShutterTickingSoundS2CP::cameraID,
            ByteBufCodecs.FLOAT, PlayOnePerEntityShutterTickingSoundS2CP::volume,
            ByteBufCodecs.FLOAT, PlayOnePerEntityShutterTickingSoundS2CP::pitch,
            ByteBufCodecs.VAR_INT, PlayOnePerEntityShutterTickingSoundS2CP::durationTicks,
            PlayOnePerEntityShutterTickingSoundS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (entity != null) {
            OnePerEntitySoundsClient.playShutterTicking(entity, cameraID, volume, pitch, durationTicks);
        }
        return true;
    }
}
