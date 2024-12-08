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

import java.util.List;

public record CreateChromaticExposureS2CP(ExposureIdentifier identifier,
                                          List<ExposureIdentifier> layers) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("create_chromatic_exposure");
    public static final CustomPacketPayload.Type<CreateChromaticExposureS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CreateChromaticExposureS2CP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, CreateChromaticExposureS2CP::identifier,
            ExposureIdentifier.STREAM_CODEC.apply(ByteBufCodecs.list()), CreateChromaticExposureS2CP::layers,
            CreateChromaticExposureS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.createChromaticExposure(this);
        return true;
    }
}
