package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.CameraAccessor;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record StartExposureS2CP(String exposureId, CameraAccessor cameraAccessor,
                                boolean flashHasFired, int lightLevelBeforeShot) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("start_exposure");
    public static final CustomPacketPayload.Type<StartExposureS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, StartExposureS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StartExposureS2CP::exposureId,
            CameraAccessor.STREAM_CODEC, StartExposureS2CP::cameraAccessor,
            ByteBufCodecs.BOOL, StartExposureS2CP::flashHasFired,
            ByteBufCodecs.VAR_INT, StartExposureS2CP::lightLevelBeforeShot,
            StartExposureS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.startExposure(this);
        return true;
    }
}
