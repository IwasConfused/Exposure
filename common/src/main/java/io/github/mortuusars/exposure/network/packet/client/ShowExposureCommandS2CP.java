package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.frame.Photographer;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public record ShowExposureCommandS2CP(List<Frame> frames,
                                      boolean negative) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("show_exposure_command");
    public static final CustomPacketPayload.Type<ShowExposureCommandS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ShowExposureCommandS2CP> STREAM_CODEC = StreamCodec.composite(
            Frame.STREAM_CODEC.apply(ByteBufCodecs.list()), ShowExposureCommandS2CP::frames,
            ByteBufCodecs.BOOL, ShowExposureCommandS2CP::negative,
            ShowExposureCommandS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static ShowExposureCommandS2CP identifier(ExposureIdentifier identifier, boolean negative) {
        Frame frame = new Frame(identifier, ExposureType.COLOR, Photographer.EMPTY, Collections.emptyList(), CustomData.EMPTY);
        return new ShowExposureCommandS2CP(List.of(frame), negative);
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.showExposure(this);
        return true;
    }
}
