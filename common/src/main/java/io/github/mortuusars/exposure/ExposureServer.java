package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureStorage;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureSender;
import io.github.mortuusars.exposure.warehouse.server.ServersideExposureReceiver;
import net.minecraft.server.MinecraftServer;

public class ExposureServer {
    private static ServersideExposureStorage exposureStorage;
    private static ServersideExposureSender exposureSender;
    private static ServersideExposureReceiver exposureReceiver;

    public static void init(MinecraftServer server) {
        exposureStorage = new ServersideExposureStorage(server);
        exposureSender = new ServersideExposureSender();
        exposureReceiver = new ServersideExposureReceiver(exposureStorage);
    }

    public static ServersideExposureStorage exposureStorage() {
        return exposureStorage;
    }
    public static ServersideExposureSender exposureSender() {
        return exposureSender;
    }
    public static ServersideExposureReceiver exposureReceiver() {
        return exposureReceiver;
    }

    public static ExposureData getExposure(ExposureIdentifier identifier) {
        //TODO: return missing image if not found
        return exposureStorage().get(identifier);
    }

    public static void awaitExposure(ExposureIdentifier identifier, ExposureType type, String creator) {
        exposureReceiver().waitForExposure(identifier, type, creator);
    }
}
