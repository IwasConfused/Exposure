package io.github.mortuusars.exposure.warehouse.client;

import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.warehouse.ExposureClientData;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ExposureDataPartC2SP;
import io.github.mortuusars.exposure.util.ByteArraySplitter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClientsideExposureUploader {
    public static final int TO_SERVER_PACKET_SPLIT_THRESHOLD = 256_000;

    public void uploadToServer(ExposureIdentifier identifier, ExposureClientData exposureClientData) {
        ByteBuf buffer = Unpooled.buffer();
        ExposureClientData.STREAM_CODEC.encode(buffer, exposureClientData);
        byte[] bytes = buffer.array();

        byte[][] parts = ByteArraySplitter.splitToParts(bytes, TO_SERVER_PACKET_SPLIT_THRESHOLD);

        for (int i = 0; i < parts.length; i++) {
            ExposureDataPartC2SP packet = new ExposureDataPartC2SP(identifier, parts[i], i == parts.length - 1);
            Packets.sendToServer(packet);
        }
    }
}
