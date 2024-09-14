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

public record CreateChromaticExposureS2CP(ExposureIdentifier red,
                                          ExposureIdentifier green,
                                          ExposureIdentifier blue,
                                          String chromaticExposureId) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("create_chromatic_exposure");
    public static final CustomPacketPayload.Type<CreateChromaticExposureS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CreateChromaticExposureS2CP> STREAM_CODEC = StreamCodec.composite(
            ExposureIdentifier.STREAM_CODEC, CreateChromaticExposureS2CP::red,
            ExposureIdentifier.STREAM_CODEC, CreateChromaticExposureS2CP::green,
            ExposureIdentifier.STREAM_CODEC, CreateChromaticExposureS2CP::blue,
            ByteBufCodecs.STRING_UTF8, CreateChromaticExposureS2CP::chromaticExposureId,
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
