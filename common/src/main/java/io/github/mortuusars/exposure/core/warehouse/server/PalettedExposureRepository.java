package io.github.mortuusars.exposure.core.warehouse.server;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import io.github.mortuusars.exposure.core.warehouse.RequestedPalettedExposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ExposureDataChangedS2CP;
import io.github.mortuusars.exposure.network.packet.client.ExposureDataResponseS2CP;
import io.github.mortuusars.exposure.core.warehouse.CapturedExposure;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
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

    protected final Map<ServerPlayer, ExpectedExposures> expectedExposures = new HashMap<>();

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

    public RequestedPalettedExposure loadExposure(ExposureIdentifier identifier) {
        Preconditions.checkArgument(!identifier.isEmpty(),
                "Empty Identifier cannot be used to get an exposure data.");
        Preconditions.checkArgument(identifier.isId(),
                "Identifier: '%s' cannot be used to get an exposure data. Only ID is supported.");

        @Nullable PalettedExposure palettedExposure = dataStorage.get(PalettedExposure.factory(), EXPOSURES_DIRECTORY_NAME + "/" + identifier.id().orElseThrow());

        if (palettedExposure == null) {
            File filepath = exposuresFolderPath.resolve(identifier.id().orElseThrow() + ".dat").toFile();
            if (!filepath.exists()) {
                LOGGER.error("Exposure '{}' was not loaded. File '{}' does not exist.", identifier.getId(), filepath);
                return RequestedPalettedExposure.NOT_FOUND;
            }

            LOGGER.error("Exposure '{}' was not loaded. Check above messages for errors.", identifier.getId());
            return RequestedPalettedExposure.CANNOT_LOAD;
        }

        return RequestedPalettedExposure.success(palettedExposure);
    }

    public void saveExposure(ExposureIdentifier identifier, PalettedExposure data) {
        Preconditions.checkArgument(!identifier.isEmpty(), "Empty Identifier cannot be used to save exposure.");
        Preconditions.checkArgument(identifier.isId(),
                "Identifier: '%s' cannot be used to save exposure. Only ID is supported.");

        if (ensureExposuresDirectoryExists()) {
            String saveDataName = EXPOSURES_DIRECTORY_NAME + "/" + identifier.id().orElseThrow();
            dataStorage.set(saveDataName, data);
            data.setDirty();
            Packets.sendToAllClients(new ExposureDataChangedS2CP(identifier));
        }
    }

    public void expect(ServerPlayer player, ExposureIdentifier identifier, PalettedExposure.Tag exposureMetadata) {
        ExpectedExposures expected = expectedExposures.computeIfAbsent(player, pl -> new ExpectedExposures());
        expected.add(identifier, exposureMetadata);
    }

    public void handleClientRequest(ServerPlayer player, ExposureIdentifier identifier) {
        if (isOverRequestLimit(player)) {
            LOGGER.error("Disconnecting player '{}': too many exposures requested. Max 200/second.",
                    player.getScoreboardName());
            player.disconnect();
            return;
        }

        RequestedPalettedExposure result;

        if (identifier.isEmpty()) {
            LOGGER.error("Empty Identifier cannot be used to get an exposure data. Player: '{}'",
                    player.getScoreboardName());
            result = RequestedPalettedExposure.INVALID_IDENTIFIER;
        } else if (!identifier.isId()) {
            LOGGER.error("Identifier: '{}' cannot be used to get an exposure data. Only ID is supported. Player: '{}'",
                    identifier, player.getScoreboardName());
            result = RequestedPalettedExposure.INVALID_IDENTIFIER;
        } else {
            result = loadExposure(identifier);
        }

        Packets.sendToClient(new ExposureDataResponseS2CP(identifier, result), player);
    }

    public void handleClientUpload(ServerPlayer player, ExposureIdentifier identifier, CapturedExposure clientExposureData) {
        if (identifier.isEmpty()) {
            LOGGER.error("Empty Identifier cannot be used to save clientExposureData. Player: '{}'", player.getScoreboardName());
            return;
        }

        if (!identifier.isId()) {
            LOGGER.error("Identifier: '{}' cannot be used to save clientExposureData. Only ID is supported. Player: '{}'",
                    identifier, player.getScoreboardName());
            return;
        }

        @Nullable ExpectedExposures expectedExposures = this.expectedExposures.get(player);
        if (expectedExposures == null || !expectedExposures.contains(identifier)) {
            LOGGER.error("Unexpected upload from player '{}' with ID '{}'. Discarding.",
                    player.getScoreboardName(), identifier);
            return;
        }

        if (!validateClientData(clientExposureData)) {
            LOGGER.error("Uploaded client data '{}' from player '{}' with ID '{}' is not valid. Discarding.",
                    clientExposureData, player.getScoreboardName(), identifier);
            return;
        }

        PalettedExposure exposure = new PalettedExposure(
                clientExposureData.width(),
                clientExposureData.height(),
                clientExposureData.pixels(),
                clientExposureData.palette(),
                expectedExposures.get(identifier).withFromFileSetTo(clientExposureData.isFromFile()));

        saveExposure(identifier, exposure);
        expectedExposures.remove(identifier);
        LOGGER.debug("Saved exposure '{}' uploaded by '{}'.", identifier, player.getScoreboardName());
    }

    protected boolean validateClientData(CapturedExposure capturedExposure) {
        return capturedExposure.width() > 0 && capturedExposure.height() > 0
                && capturedExposure.pixels().length == capturedExposure.width() * capturedExposure.height();
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
