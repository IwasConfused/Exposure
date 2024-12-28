package io.github.mortuusars.exposure.core.warehouse.client;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.warehouse.RequestedExposureStatus;
import io.github.mortuusars.exposure.core.warehouse.RequestedPalettedExposure;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.mortuusars.exposure.core.warehouse.RequestedExposureStatus.*;

public class ExposureStore {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ExposureRequester requester = new ExposureRequester(200);

    private final Map<ExposureIdentifier, RequestedPalettedExposure> exposures = new ConcurrentHashMap<>();

    public RequestedPalettedExposure getOrRequest(ExposureIdentifier identifier) {
        Preconditions.checkArgument(identifier.isId(),
                "Identifier: '%s' cannot be used to get an exposure data. Only ID is supported.");

        RequestedPalettedExposure exposure = exposures.getOrDefault(identifier, RequestedPalettedExposure.NOT_REQUESTED);

        if (exposure.is(SUCCESS)) {
            return exposure;
        }

        if (exposure.is(NOT_REQUESTED)) {
            return request(identifier);
        }

        if (exposure.is(AWAITED) && requester.isTimedOut(identifier)) {
            LOGGER.info("Exposure '{}' was not received in {} seconds. Requesting again.", identifier.getId(), requester.getTimeoutSeconds());
            return request(identifier);
        }

        return exposure;
    }

    public void receive(ExposureIdentifier identifier, RequestedPalettedExposure result) {
        exposures.put(identifier, result);
        requester.requestFulfilled(identifier);

        RequestedExposureStatus status = result.getStatus();
        if (status != SUCCESS && status != NEEDS_REFRESH) {
            LOGGER.error("Received unsuccessful exposure '{}'. Status: {}", identifier.getId(), status);
        }
    }

    public void refresh(ExposureIdentifier identifier) {
        exposures.computeIfPresent(identifier, (id, exposure) ->
                exposure.is(SUCCESS) ? RequestedPalettedExposure.needsRefresh(exposure) : exposure);
        requester.refresh(identifier);
    }

    public void clear() {
        exposures.clear();
        requester.clear();
    }

    private RequestedPalettedExposure request(ExposureIdentifier identifier) {
        ExposureRequester.Status requestStatus = requester.request(identifier);
        RequestedPalettedExposure requestResult = RequestedPalettedExposure.fromRequestStatus(requestStatus);
        exposures.put(identifier, requestResult);
        return requestResult;
    }
}
