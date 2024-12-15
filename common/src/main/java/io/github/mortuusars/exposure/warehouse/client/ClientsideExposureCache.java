package io.github.mortuusars.exposure.warehouse.client;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.QueryExposureDataC2SP;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClientsideExposureCache {
    protected final Map<ExposureIdentifier, ExposureData> cache = new HashMap<>();
    protected final Set<ExposureIdentifier> queriedExposureIds = new HashSet<>();
    protected final Set<ExposureIdentifier> waitingExposureIds = new HashSet<>();

    public Optional<ExposureData> getOrQuery(ExposureIdentifier identifier) {
        ExposureData exposureData = cache.get(identifier);

        if (exposureData == null && !waitingExposureIds.contains(identifier) && !queriedExposureIds.contains(identifier)) {
            Packets.sendToServer(new QueryExposureDataC2SP(identifier));
            queriedExposureIds.add(identifier);
        }

        return Optional.ofNullable(exposureData);
    }

    public ExposureData getOrQueryAndEmpty(ExposureIdentifier identifier) {
        Preconditions.checkArgument(identifier.isId(), "Identifier: '%s' is cannot be used to get or query an exposure data. Only ID is supported.");

        ExposureData exposureData = cache.get(identifier);

        if (exposureData == null) {
            if (!waitingExposureIds.contains(identifier) && !queriedExposureIds.contains(identifier)) {
                Packets.sendToServer(new QueryExposureDataC2SP(identifier));
                queriedExposureIds.add(identifier);
            }
            return ExposureData.EMPTY;
        }

        return exposureData;
    }

    public void put(ExposureIdentifier identifier, @NotNull ExposureData data) {
        cache.put(identifier, data);
        queriedExposureIds.remove(identifier);
        waitingExposureIds.remove(identifier);
    }

    public void putOnWaitingList(ExposureIdentifier identifier) {
        waitingExposureIds.add(identifier);
    }

    public List<ExposureIdentifier> getAllIds() {
        return cache.keySet().stream().toList();
    }

    public void remove(ExposureIdentifier identifier) {
        cache.remove(identifier);
        queriedExposureIds.remove(identifier);
        waitingExposureIds.remove(identifier);
    }

    public void clear() {
        cache.clear();
        queriedExposureIds.clear();
        waitingExposureIds.clear();
    }
}
