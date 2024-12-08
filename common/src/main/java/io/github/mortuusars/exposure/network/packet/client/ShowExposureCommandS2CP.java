package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
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

public record ShowExposureCommandS2CP(ExposureIdentifier identifier,
                                      boolean negative,
                                      boolean showLatest) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("show_exposure_command");
    public static final CustomPacketPayload.Type<ShowExposureCommandS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ShowExposureCommandS2CP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, ShowExposureCommandS2CP::identifier,
            ByteBufCodecs.BOOL, ShowExposureCommandS2CP::negative,
            ByteBufCodecs.BOOL, ShowExposureCommandS2CP::showLatest,
            ShowExposureCommandS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static ShowExposureCommandS2CP latest(boolean negative) {
        return new ShowExposureCommandS2CP(ExposureIdentifier.EMPTY, negative, true);
    }

    public static ShowExposureCommandS2CP identifier(ExposureIdentifier identifier, boolean negative) {
        return new ShowExposureCommandS2CP(identifier, negative, false);
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.showExposure(this);
        return true;
    }
}
