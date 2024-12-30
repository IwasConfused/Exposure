package io.github.mortuusars.exposure.core.warehouse.client;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.warehouse.RequestedExposureStatus;
import io.github.mortuusars.exposure.core.warehouse.RequestedPalettedExposure;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.mortuusars.exposure.core.warehouse.RequestedExposureStatus.*;

public class ExposureStore {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ExposureRequester requester = new ExposureRequester(200);

    private final Map<String, RequestedPalettedExposure> exposures = new ConcurrentHashMap<>();

    public RequestedPalettedExposure getOrRequest(@NotNull String id) {
        if (StringUtil.isBlank(id)) {
            return RequestedPalettedExposure.INVALID_ID;
        }

        RequestedPalettedExposure exposure = exposures.getOrDefault(id, RequestedPalettedExposure.NOT_REQUESTED);

        if (exposure.is(SUCCESS)) {
            return exposure;
        }

        if (exposure.is(NOT_REQUESTED)) {
            return request(id);
        }

        if (exposure.is(AWAITED) && requester.isTimedOut(id)) {
            LOGGER.info("Exposure '{}' was not received in {} seconds. Requesting again.", id, requester.getTimeoutSeconds());
            return request(id);
        }

        return exposure;
    }

    public void receive(@NotNull String id, RequestedPalettedExposure result) {
        if (StringUtil.isBlank(id)) {
            return;
        }

        exposures.put(id, result);
        requester.requestFulfilled(id);

        RequestedExposureStatus status = result.getStatus();
        if (status != SUCCESS && status != NEEDS_REFRESH) {
            LOGGER.error("Received unsuccessful exposure '{}'. Status: {}", id, status);
        }
    }

    public void refresh(String id) {
        exposures.compute(id, (identifier, exposure) -> {
            if (exposure != null && exposure.is(SUCCESS)) {
                return RequestedPalettedExposure.needsRefresh(exposure);
            }
            return null;
        });
        requester.refresh(id);
    }

    public void clear() {
        exposures.clear();
        requester.clear();
    }

    private RequestedPalettedExposure request(String id) {
        ExposureRequester.Status requestStatus = requester.request(id);
        RequestedPalettedExposure requestResult = RequestedPalettedExposure.fromRequestStatus(requestStatus);
        exposures.put(id, requestResult);
        return requestResult;
    }
}
