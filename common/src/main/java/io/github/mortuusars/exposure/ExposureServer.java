package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.foundation.warehouse.server.ExposureVault;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.warehouse.server.ExposureFrameHistory;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureStorage;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureSender;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureReceiver;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class ExposureServer {

    private static ExposureVault vault;

    private static ServersideExposureStorage exposureStorage;
    private static ServersideExposureSender exposureSender;
    private static ServersideExposureReceiver exposureReceiver;

    private static ExposureFrameHistory exposureFrameHistory;

    public static void init(MinecraftServer server) {
        vault = new ExposureVault(server);

        exposureStorage = new ServersideExposureStorage(server);
        exposureSender = new ServersideExposureSender();
        exposureReceiver = new ServersideExposureReceiver(exposureStorage);

        exposureFrameHistory = ExposureFrameHistory.loadOrCreate(server);
    }

    public static ExposureVault vault() {
        return ensureInitialized(vault);
    }

    public static ServersideExposureStorage exposureStorage() {
        return ensureInitialized(exposureStorage);
    }

    public static ServersideExposureSender exposureSender() {
        return ensureInitialized(exposureSender);
    }

    public static ServersideExposureReceiver exposureReceiver() {
        return ensureInitialized(exposureReceiver);
    }

    public static ExposureFrameHistory frameHistory() {
        return ensureInitialized(exposureFrameHistory);
    }

    public static ExposureData getExposure(ExposureIdentifier identifier) {
        return exposureStorage().get(identifier);
    }

    public static void awaitExposure(ExposureIdentifier identifier, ExposureType type, String creator) {
        exposureReceiver().waitForExposure(identifier, type, creator);
    }

    private static <T> T ensureInitialized(@Nullable T obj) {
        Preconditions.checkNotNull(obj, "Cannot get a field in ExposureServer: server is not initialized yet.");
        return obj;
    }
}
