package io.github.mortuusars.exposure.warehouse.client;

import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.QueryExposureDataC2SP;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClientsideExposureCache {
    protected final Map<String, ExposureData> cache = new HashMap<>();
    protected final Set<String> queriedExposureIds = new HashSet<>();
    protected final Set<String> waitingExposureIds = new HashSet<>();

    public Optional<ExposureData> getOrQuery(String exposureId) {
        ExposureData exposureData = cache.get(exposureId);

        if (exposureData == null && !waitingExposureIds.contains(exposureId) && !queriedExposureIds.contains(exposureId)) {
            Packets.sendToServer(new QueryExposureDataC2SP(exposureId));
            queriedExposureIds.add(exposureId);
        }

        return Optional.ofNullable(exposureData);
    }

    public ExposureData getOrQueryAndEmpty(String exposureId) {
        ExposureData exposureData = cache.get(exposureId);

        if (exposureData == null) {
            if (!waitingExposureIds.contains(exposureId) && !queriedExposureIds.contains(exposureId)) {
                Packets.sendToServer(new QueryExposureDataC2SP(exposureId));
                queriedExposureIds.add(exposureId);
            }
            return ExposureData.EMPTY;
        }

        return exposureData;
    }

    public void put(String exposureId, @NotNull ExposureData data) {
        cache.put(exposureId, data);
        queriedExposureIds.remove(exposureId);
        waitingExposureIds.remove(exposureId);
    }

    public void putOnWaitingList(String exposureId) {
        waitingExposureIds.add(exposureId);
    }

    public List<String> getAllIds() {
        return cache.keySet().stream().toList();
    }

    public void remove(String exposureId) {
        cache.remove(exposureId);
        queriedExposureIds.remove(exposureId);
        waitingExposureIds.remove(exposureId);
    }

    public void clear() {
        cache.clear();
        queriedExposureIds.clear();
        waitingExposureIds.clear();
    }
}
