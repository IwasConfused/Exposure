package io.github.mortuusars.exposure.warehouse.client;

public class ClientsideExposureReceiver {
//    protected final Map<ExposureIdentifier, ArrayList<byte[]>> receivedParts = new HashMap<>();
//    protected final ClientsideExposureCache exposureCache;
//
//    public ClientsideExposureReceiver(ClientsideExposureCache exposureCache) {
//        this.exposureCache = exposureCache;
//    }
//
//    public void receivePart(ExposureIdentifier identifier, byte[] partBytes, boolean isLast) {
//        ArrayList<byte[]> parts = receivedParts.compute(identifier, (key, value) -> value == null ? new ArrayList<>() : value);
//
//        parts.add(partBytes);
//
//        if (isLast) {
//            ByteBuf buffer = Unpooled.buffer();
//            for (byte[] part : parts) {
//                buffer.writeBytes(part);
//            }
//
//            ExposureData exposureData = ExposureData.STREAM_CODEC.decode(buffer);
//            receive(identifier, exposureData);
//        }
//    }
//
//    public void receive(ExposureIdentifier identifier, ExposureData exposureData) {
//        exposureCache.put(identifier, exposureData);
//        receivedParts.remove(identifier);
//    }
//
//    public void clear() {
//        receivedParts.clear();
//    }
}
