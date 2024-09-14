package io.github.mortuusars.exposure.warehouse.client;

import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientsideExposureReceiver {
    protected final Map<String, ArrayList<byte[]>> receivedParts = new HashMap<>();
    protected final ClientsideExposureCache exposureCache;

    public ClientsideExposureReceiver(ClientsideExposureCache exposureCache) {
        this.exposureCache = exposureCache;
    }

    public void receivePart(String exposureId, byte[] partBytes, boolean isLast) {
        ArrayList<byte[]> parts = receivedParts.compute(exposureId, (key, value) -> value == null ? new ArrayList<>() : value);

        parts.add(partBytes);

        if (isLast) {
            ByteBuf buffer = Unpooled.buffer();
            for (byte[] part : parts) {
                buffer.writeBytes(part);
            }

            ExposureData exposureData = ExposureData.STREAM_CODEC.decode(buffer);
            receive(exposureId, exposureData);
        }
    }

    public void receive(String exposureId, ExposureData exposureData) {
        exposureCache.put(exposureId, exposureData);
        receivedParts.remove(exposureId);
    }

    public void clear() {
        receivedParts.clear();
    }
}
