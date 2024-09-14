package io.github.mortuusars.exposure.warehouse.server;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ExposureChangedS2CP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ServersideExposureStorage {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String EXPOSURES_DIRECTORY_NAME = "exposures";

    protected final MinecraftServer server;

    protected final Supplier<DimensionDataStorage> levelStorageSupplier;
    protected final Supplier<Path> levelPathSupplier;

    public ServersideExposureStorage(MinecraftServer server) {
        this.server = server;
        this.levelStorageSupplier = () -> server.overworld().getDataStorage();
        this.levelPathSupplier = () -> server.getWorldPath(LevelResource.ROOT);
        createDirectoryIfNeeded();
    }

    public List<String> getAllExposureIds() {
        // Save exposures that are in cache and waiting to be saved:
        levelStorageSupplier.get().save();

        Path path = levelPathSupplier.get().resolve("data/" + EXPOSURES_DIRECTORY_NAME);
        File folder = path.toFile();

        @Nullable File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return Collections.emptyList();

        List<String> ids = new ArrayList<>();

        for (File file : listOfFiles) {
            if (file != null && file.isFile())
                ids.add(com.google.common.io.Files.getNameWithoutExtension(file.getName()));
        }

        return ids;
    }

    public ExposureData get(String exposureId) {
        DimensionDataStorage dataStorage = levelStorageSupplier.get();
        @Nullable ExposureData exposureData = dataStorage.get(ExposureData.factory(), getSaveId(exposureId));

        if (exposureData == null) {
            LOGGER.error("Exposure '{}' was not loaded. File does not exist or some error occurred.", exposureId);
            return ExposureData.EMPTY;
        }

        return exposureData;
    }

    public void put(String exposureId, ExposureData data) {
        if (createDirectoryIfNeeded()) {
            DimensionDataStorage dataStorage = levelStorageSupplier.get();
            dataStorage.set(getSaveId(exposureId), data);
            data.setDirty();

            //TODO: Serverside frame history
//            if (server.isDedicatedServer()) {
//                ExposureFrame frame = ExposureFrame.EMPTY
//                        .toMutable()
//                        .setIdentifier(new ExposureIdentifier(exposureId))
//                        .toImmutable();
//                CapturedFramesHistory.add(frame);
//            }
        }
    }

    /**
     * When exposure on the server changes we need to notify client to re-query it.
     * Otherwise, due to caching, client wouldn't know about the change.
     */
    public void sendExposureChanged(String exposureId) {
        Packets.sendToAllClients(new ExposureChangedS2CP(exposureId));
    }

    protected String getSaveId(String exposureId) {
        return EXPOSURES_DIRECTORY_NAME + "/" + exposureId;
    }

    protected boolean createDirectoryIfNeeded() {
        try {
            Path path = levelPathSupplier.get().resolve("data/" + EXPOSURES_DIRECTORY_NAME);
            return Files.exists(path) || path.toFile().mkdirs();
        }
        catch (Exception e) {
            LOGGER.error("Failed to create exposure storage directory: {}", e.toString());
            return false;
        }
    }
}
