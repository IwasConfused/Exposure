package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.ExposureServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//public record ExposureDataPartPacket(String exposureId,
//                                     int width,
//                                     int height,
//                                     byte[] pixelsPart,
//                                     int offset,
//                                     boolean isFromFile) implements IPacket {
//    public static final ResourceLocation ID = Exposure.resource("exposure_data_part");
//    public static final CustomPacketPayload.Type<ExposureDataPartPacket> TYPE = new CustomPacketPayload.Type<>(ID);
//
//    public static final StreamCodec<FriendlyByteBuf, ExposureDataPartPacket> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.STRING_UTF8, ExposureDataPartPacket::exposureId,
//            ByteBufCodecs.VAR_INT, ExposureDataPartPacket::width,
//            ByteBufCodecs.VAR_INT, ExposureDataPartPacket::height,
//            ByteBufCodecs.byteArray(28000), ExposureDataPartPacket::pixelsPart,
//            ByteBufCodecs.VAR_INT, ExposureDataPartPacket::offset,
//            ByteBufCodecs.BOOL, ExposureDataPartPacket::isFromFile,
//            ExposureDataPartPacket::new
//    );
//
//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
//
//    @Override
//    public boolean handle(PacketFlow flow, @Nullable Player player) {
//        IExposureReceiver receiver = flow == PacketFlow.SERVERBOUND
//                ? ExposureServer.exposureReceiver() : ExposureClient.getExposureReceiver();
//        receiver.receivePart(exposureId, width, height, pixelsPart, offset, isFromFile);
//        return true;
//    }
//}
