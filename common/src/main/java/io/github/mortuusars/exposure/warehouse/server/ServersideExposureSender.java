package io.github.mortuusars.exposure.warehouse.server;

public class ServersideExposureSender {
//    // Packet size limit from server to client is 8mb, but, from my limited testing, it causes problems sometimes.
//    // So we split if >512kb, it should be more than enough for regular-sized exposures.
//    public static final int TO_CLIENT_PACKET_SPLIT_THRESHOLD = 512_000;
//
//    public synchronized void sendTo(ExposureIdentifier identifier, ExposureData exposureData, @NotNull ServerPlayer receivingPlayer) {
//        ByteBuf buffer = Unpooled.buffer();
//        ExposureData.STREAM_CODEC.encode(buffer, exposureData);
//        byte[] bytes = buffer.array();
//
//        byte[][] parts = ByteArrayUtils.splitToParts(bytes, TO_CLIENT_PACKET_SPLIT_THRESHOLD);
//
//        for (int i = 0; i < parts.length; i++) {
//            ExposureDataPartS2CP packet = new ExposureDataPartS2CP(identifier, parts[i], i == parts.length - 1);
//            Packets.sendToClient(packet, receivingPlayer);
//        }
//    }
}
