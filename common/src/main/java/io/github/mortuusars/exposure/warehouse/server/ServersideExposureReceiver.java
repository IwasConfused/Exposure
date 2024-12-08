package io.github.mortuusars.exposure.warehouse.server;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.warehouse.ExposureClientData;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServersideExposureReceiver {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected static final int PENDING_EXPOSURE_TIMEOUT_SECONDS = 60;
    private static final int MAX_CLIENT_EXTRA_DATA_SIZE = 64_000;

    protected final Map<ExposureIdentifier, PendingExposure> pendingExposures = new HashMap<>();
    protected final Map<ExposureIdentifier, ArrayList<byte[]>> receivedParts = new HashMap<>();

    protected final ServersideExposureStorage exposureStorage;

    public ServersideExposureReceiver(ServersideExposureStorage exposureStorage) {
        this.exposureStorage = exposureStorage;
    }

    public void waitForExposure(ExposureIdentifier identifier, ExposureType type, String creator, CompoundTag extraData) {
        cleanupTimedOutExposures();
        pendingExposures.put(identifier, new PendingExposure(type, creator, UnixTimestamp.Seconds.now(), extraData));
    }

    public void waitForExposure(ExposureIdentifier identifier, ExposureType type, String creator) {
        waitForExposure(identifier, type, creator, new CompoundTag());
    }

    public void receivePart(ExposureIdentifier identifier, byte[] partBytes, boolean isLast) {
        @Nullable PendingExposure pendingExposure = pendingExposures.get(identifier);
        if (pendingExposure == null) {
            LOGGER.warn("Received unexpected exposure part with exposureId '{}'. Discarding.", identifier);
            receivedParts.remove(identifier);
            return;
        }

        ArrayList<byte[]> parts = receivedParts.compute(identifier, (key, value) -> value == null ? new ArrayList<>() : value);

        parts.add(partBytes);

        if (isLast) {
            ByteBuf buffer = Unpooled.buffer();
            for (byte[] part : parts) {
                buffer.writeBytes(part);
            }

            ExposureClientData exposureData = ExposureClientData.STREAM_CODEC.decode(buffer);
            receive(identifier, exposureData);
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

    public void receive(ExposureIdentifier identifier, ExposureClientData exposureClientData) {
        cleanupTimedOutExposures();

        @Nullable PendingExposure pendingExposure = pendingExposures.get(identifier);
        if (pendingExposure == null) {
            LOGGER.warn("Received unexpected exposure with identifier '{}'. Discarding.", identifier);
            receivedParts.remove(identifier);
            return;
        }

        receivedParts.remove(identifier);
        pendingExposures.remove(identifier);
        ExposureData exposureData = createExposureData(pendingExposure, exposureClientData);
        exposureStorage.put(identifier, exposureData);
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

        return new ExposureData(clientData.width(), clientData.height(), clientData.pixels(),
                clientData.palette(), pendingExposure.type(), pendingExposure.creator(),
                pendingExposure.unixTimestamp(), clientData.fromFile(), extraData, false);
    }

    protected boolean isTimedOut(@NotNull PendingExposure exposure) {
        return UnixTimestamp.Seconds.now() - exposure.unixTimestamp() > PENDING_EXPOSURE_TIMEOUT_SECONDS;
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
