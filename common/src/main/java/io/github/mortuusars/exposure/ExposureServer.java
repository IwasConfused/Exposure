package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.core.warehouse.server.PalettedExposureRepository;
import io.github.mortuusars.exposure.core.warehouse.server.ExposureFrameHistory;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.data.Lenses;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class ExposureServer {
    private static final ColorPalettes ColorPalettes = new ColorPalettes();
    private static final Lenses lensesManager = new Lenses();

    private static PalettedExposureRepository vault;
    private static ExposureFrameHistory exposureFrameHistory;

    public static void init(MinecraftServer server) {
        vault = new PalettedExposureRepository(server);
        exposureFrameHistory = ExposureFrameHistory.loadOrCreate(server);
    }

    public static ColorPalettes colorPalettes() {
        return ensureInitialized(ColorPalettes);
    }

    public static Lenses lenses() {
        return ensureInitialized(lensesManager);
    }

    // --

    public static PalettedExposureRepository exposureRepository() {
        return ensureInitialized(vault);
    }

    public static ExposureFrameHistory frameHistory() {
        return ensureInitialized(exposureFrameHistory);
    }

    // --

    private static <T> T ensureInitialized(@Nullable T obj) {
        Preconditions.checkNotNull(obj, "Cannot get a field in ExposureServer: server is not initialized yet.");
        return obj;
    }
}
