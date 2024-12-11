package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.warehouse.server.ExposureFrameHistory;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureStorage;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureSender;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureReceiver;
import net.minecraft.server.MinecraftServer;

public class ExposureServer {
    private static ServersideExposureStorage exposureStorage;
    private static ServersideExposureSender exposureSender;
    private static ServersideExposureReceiver exposureReceiver;

    private static ExposureFrameHistory exposureFrameHistory;

    public static void init(MinecraftServer server) {
        exposureStorage = new ServersideExposureStorage(server);
        exposureSender = new ServersideExposureSender();
        exposureReceiver = new ServersideExposureReceiver(exposureStorage);

        exposureFrameHistory = ExposureFrameHistory.loadOrCreate(server);
    }

    public static ServersideExposureStorage exposureStorage() {
        Preconditions.checkNotNull(exposureStorage, "Cannot get exposure storage: server is not initialized yet.");
        return exposureStorage;
    }
    public static ServersideExposureSender exposureSender() {
        Preconditions.checkNotNull(exposureSender, "Cannot get exposure sender: server is not initialized yet.");
        return exposureSender;
    }
    public static ServersideExposureReceiver exposureReceiver() {
        Preconditions.checkNotNull(exposureReceiver, "Cannot get exposure receiver: server is not initialized yet.");
        return exposureReceiver;
    }

    public static ExposureFrameHistory exposureFrameHistory() {
        Preconditions.checkNotNull(exposureFrameHistory, "Cannot get exposure frame history: server is not initialized yet.");
        return exposureFrameHistory;
    }

    public static ExposureData getExposure(ExposureIdentifier identifier) {
        return exposureStorage().get(identifier);
    }

    public static void awaitExposure(ExposureIdentifier identifier, ExposureType type, String creator) {
        exposureReceiver().waitForExposure(identifier, type, creator);
    }
}
