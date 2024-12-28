package io.github.mortuusars.exposure.core.warehouse.server;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.warehouse.RequestedPalettedExposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ExposureDataChangedS2CP;
import io.github.mortuusars.exposure.network.packet.client.ExposureDataResponseS2CP;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PalettedExposureRepository {
    public static final String EXPOSURES_DIRECTORY_NAME = "exposures";

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final MinecraftServer server;
    protected final DimensionDataStorage dataStorage;
    protected final Path worldFolderPath;
    protected final Path exposuresFolderPath;

    protected final Map<ServerPlayer, Set<String>> expectedExposures = new HashMap<>();

//    protected final Map<ServerPlayer, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    public PalettedExposureRepository(MinecraftServer server) {
        this.server = server;
        this.dataStorage = server.overworld().getDataStorage();
        this.worldFolderPath = server.getWorldPath(LevelResource.ROOT);
        this.exposuresFolderPath = worldFolderPath.resolve("data/" + EXPOSURES_DIRECTORY_NAME);
    }

    public List<ExposureIdentifier> getAllIdentifiers() {
        // Save exposures that are in cache and waiting to be saved:
        dataStorage.save();

        File folder = exposuresFolderPath.toFile();

        @Nullable File[] filesList = folder.listFiles();
        if (filesList == null) {
            return Collections.emptyList();
        }

        List<ExposureIdentifier> ids = new ArrayList<>();

        for (File file : filesList) {
            if (file != null && file.isFile()) {
                String filename = com.google.common.io.Files.getNameWithoutExtension(file.getName());
                ids.add(ExposureIdentifier.id(filename));
            }
        }

        return ids;
    }

    public RequestedPalettedExposure loadExposure(@NotNull String id) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkArgument(!StringUtil.isBlank(id), "Cannot load exposure: id is empty.");

        String name = EXPOSURES_DIRECTORY_NAME + "/" + id;
        @Nullable PalettedExposure palettedExposure = dataStorage.get(PalettedExposure.factory(), name);

        if (palettedExposure == null) {
            File filepath = exposuresFolderPath.resolve(id + ".dat").toFile();
            if (!filepath.exists()) {
                LOGGER.error("Exposure '{}' was not loaded. File '{}' does not exist.", id, filepath);
                return RequestedPalettedExposure.NOT_FOUND;
            }

            LOGGER.error("Exposure '{}' was not loaded. Check above messages for errors.", id);
            return RequestedPalettedExposure.CANNOT_LOAD;
        }

        return RequestedPalettedExposure.success(palettedExposure);
    }

    public void saveExposure(@NotNull String id, PalettedExposure data) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkArgument(!StringUtil.isBlank(id), "Cannot save exposure: id is empty.");

        if (ensureExposuresDirectoryExists()) {
            String saveDataName = EXPOSURES_DIRECTORY_NAME + "/" + id;
            dataStorage.set(saveDataName, data);
            data.setDirty();
            Packets.sendToAllClients(new ExposureDataChangedS2CP(id));
        }
    }

    public void expect(ServerPlayer player, String id) {
        Preconditions.checkArgument(!StringUtil.isBlank(id), "id cannot be null or empty.");
        Set<String> exposures = expectedExposures.computeIfAbsent(player, pl -> new HashSet<>());
        exposures.add(id);
    }

    public void handleClientRequest(ServerPlayer player, String id) {
        if (isOverRequestLimit(player)) {
            LOGGER.error("Disconnecting player '{}': too many exposures requested. Max 200/second.",
                    player.getScoreboardName());
            player.disconnect();
            return;
        }

        RequestedPalettedExposure result;

        if (StringUtil.isBlank(id)) {
            LOGGER.error("Null or empty id cannot be used to get an exposure data. Player: '{}'", player.getScoreboardName());
            result = RequestedPalettedExposure.INVALID_ID;
        } else {
            result = loadExposure(id);
        }

        Packets.sendToClient(new ExposureDataResponseS2CP(id, result), player);
    }

    public void handleClientUpload(ServerPlayer player, String id, PalettedExposure exposure) {
        if (StringUtil.isBlank(id)) {
            LOGGER.error("Null or empty id cannot be used to save captured exposure. Player: '{}'", player.getScoreboardName());
            return;
        }

        if (!isExposureExpected(player, id)) {
            LOGGER.error("Unexpected upload from player '{}' with ID '{}'. Discarding.", player.getScoreboardName(), id);
            return;
        }

        saveExposure(id, exposure);
        expectedExposures.get(player).remove(id);
        LOGGER.debug("Saved exposure '{}' uploaded by '{}'.", id, player.getScoreboardName());
    }

    protected boolean isExposureExpected(ServerPlayer player, String id) {
        return expectedExposures.containsKey(player) && expectedExposures.get(player).contains(id);
    }

    protected boolean isOverRequestLimit(ServerPlayer player) {
        //TODO: Probably a good idea to keep this disabled for now, as I'm not sure it'll not cause any problems.
        // It can be enabled later if someone reports it being an issue.
        /*
        if (server.isDedicatedServer()) {
            RateLimiter limiter = rateLimiters.computeIfAbsent(player, pl -> new RateLimiter(200, 200));
            return !limiter.tryConsume();
        }
        */
        return false;
    }

    protected boolean ensureExposuresDirectoryExists() {
        try {
            return Files.exists(exposuresFolderPath) || exposuresFolderPath.toFile().mkdirs();
        } catch (Exception e) {
            LOGGER.error("Failed to create exposure storage directory: {}", e.toString());
            return false;
        }
    }
}
