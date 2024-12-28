package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.foundation.warehouse.server.ExposureVault;
import io.github.mortuusars.exposure.foundation.warehouse.server.ExposureFrameHistory;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class ExposureServer {
    private static ExposureVault vault;
    private static ExposureFrameHistory exposureFrameHistory;

    public static void init(MinecraftServer server) {
        vault = new ExposureVault(server);
        exposureFrameHistory = ExposureFrameHistory.loadOrCreate(server);
    }

    public static ExposureVault vault() {
        return ensureInitialized(vault);
    }

    public static ExposureFrameHistory frameHistory() {
        return ensureInitialized(exposureFrameHistory);
    }

    private static <T> T ensureInitialized(@Nullable T obj) {
        Preconditions.checkNotNull(obj, "Cannot get a field in ExposureServer: server is not initialized yet.");
        return obj;
    }
}
