package io.github.mortuusars.exposure.warehouse.server;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.warehouse.ExposureClientData;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServersideExposureReceiver {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected static final int PENDING_EXPOSURE_TIMEOUT = 60;
    private static final int MAX_CLIENT_EXTRA_DATA_SIZE = 64_000;

    protected final Map<String, PendingExposure> pendingExposures = new HashMap<>();
    protected final Map<String, ArrayList<byte[]>> receivedParts = new HashMap<>();

    protected final ServersideExposureStorage exposureStorage;

    public ServersideExposureReceiver(ServersideExposureStorage exposureStorage) {
        this.exposureStorage = exposureStorage;
    }

    public void waitForExposure(String exposureId, ExposureType type, String creator, CompoundTag extraData) {
        cleanupTimedOutExposures();
        pendingExposures.put(exposureId, new PendingExposure(type, creator, UnixTimestamp.Seconds.now(), extraData));
    }

    public void waitForExposure(String exposureId, ExposureType type, String creator) {
        waitForExposure(exposureId, type, creator, new CompoundTag());
    }

    public void receivePart(String exposureId, byte[] partBytes, boolean isLast) {
        @Nullable PendingExposure pendingExposure = pendingExposures.get(exposureId);
        if (pendingExposure == null) {
            LOGGER.warn("Received unexpected exposure part with exposureId '{}'. Discarding.", exposureId);
            receivedParts.remove(exposureId);
            return;
        }

        ArrayList<byte[]> parts = receivedParts.compute(exposureId, (key, value) -> value == null ? new ArrayList<>() : value);

        parts.add(partBytes);

        if (isLast) {
            ByteBuf buffer = Unpooled.buffer();
            for (byte[] part : parts) {
                buffer.writeBytes(part);
            }

            ExposureClientData exposureData = ExposureClientData.STREAM_CODEC.decode(buffer);
            receive(exposureId, exposureData);
        }
    }

//    //TODO: Neoforge seems to have a packet splitter built in. Should try to send large packet to test it.
//    // And if it works - use totalParts only on fabric.
//    public void receivePart(String exposureId, int width, int height, byte[] pixelsPart, int offset, boolean isFromFile) {
//        byte[] pixels = receivedParts.compute(exposureId, (key, data) ->
//                data == null ? new byte[width * height] : data);
//
//        System.arraycopy(pixelsPart, 0, pixels, offset, pixelsPart.length);
//        receivedParts.put(exposureId, pixels);
//
//        boolean receivedAllParts = offset + pixelsPart.length >= pixels.length;
//        if (receivedAllParts) {
//            receive(exposureId, width, height, pixels, isFromFile);
//        }
//    }

    public void receive(String exposureId, ExposureClientData exposureClientData) {
        cleanupTimedOutExposures();

        @Nullable PendingExposure pendingExposure = pendingExposures.get(exposureId);
        if (pendingExposure == null) {
            LOGGER.warn("Received unexpected exposure with exposureId '{}'. Discarding.", exposureId);
            receivedParts.remove(exposureId);
            return;
        }

        receivedParts.remove(exposureId);
        pendingExposures.remove(exposureId);
        ExposureData exposureData = createExposureData(pendingExposure, exposureClientData);
        exposureStorage.put(exposureId, exposureData);
    }

    protected ExposureData createExposureData(PendingExposure pendingExposure, ExposureClientData clientData) {
        CompoundTag extraData = pendingExposure.extraData;

        CompoundTag extraDataFromClient = clientData.extraData();
        int clientDataBytesCount = extraDataFromClient.sizeInBytes();

        if (clientDataBytesCount <= MAX_CLIENT_EXTRA_DATA_SIZE) {
            extraData.merge(extraDataFromClient);
        }
        else {
            LOGGER.warn("Refusing to accept extraData from client: size is too large. '{}bytes'. Max: '{}'",
                    clientDataBytesCount, MAX_CLIENT_EXTRA_DATA_SIZE);
        }

        return new ExposureData(clientData.width(), clientData.height(), clientData.pixels(), pendingExposure.type(),
                pendingExposure.creator(), pendingExposure.unixTimestamp(), clientData.fromFile(), extraData, false);
    }

    protected boolean isTimedOut(PendingExposure exposure) {
        if (exposure == null) return true;
        return UnixTimestamp.Seconds.now() - exposure.unixTimestamp() > PENDING_EXPOSURE_TIMEOUT;
    }

    protected void cleanupTimedOutExposures() {
        pendingExposures.entrySet().removeIf(entry -> {
            PendingExposure exposure = entry.getValue();
            if (isTimedOut(exposure)) {
                LOGGER.warn("Exposure with exposureId '{}' was not received in time.", entry.getKey());
                return true;
            }
            return false;
        });
    }

    public record PendingExposure(ExposureType type, String creator, long unixTimestamp, CompoundTag extraData) {}
}
