package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.sound.OnePerEntitySoundsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PlayOnePerEntityShutterTickingSoundS2CP(CameraAccessor cameraAccessor, UUID sourceEntityID,
                                                      float volume, float pitch, int durationTicks) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("play_one_per_entity_shutter_ticking_sound");
    public static final Type<PlayOnePerEntityShutterTickingSoundS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayOnePerEntityShutterTickingSoundS2CP> STREAM_CODEC = StreamCodec.composite(
            CameraAccessor.STREAM_CODEC, PlayOnePerEntityShutterTickingSoundS2CP::cameraAccessor,
            UUIDUtil.STREAM_CODEC, PlayOnePerEntityShutterTickingSoundS2CP::sourceEntityID,
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
        if (Minecraft.getInstance().level != null) {
            @Nullable Entity sourceEntity = Minecraft.getInstance().level.getEntities().get(sourceEntityID);
            if (sourceEntity != null) {
                Minecraft.getInstance().execute(() ->
                        OnePerEntitySoundsClient.playShutterTickingSound(cameraAccessor, sourceEntity, volume, pitch, durationTicks));
            } else {
                Exposure.LOGGER.debug("Cannot play OnePerEntity sound. Source Entity was not found by it's UUID.");
            }
        }

        return true;
    }
}
