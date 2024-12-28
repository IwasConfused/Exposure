package io.github.mortuusars.exposure.client.capture.saving;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.warehouse.PalettedExposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ExposureClientDataC2SP;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

public class PalettedExposureUploader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void upload(String id, PalettedExposure exposure) {
        Preconditions.checkArgument(!StringUtil.isBlank(id), "Cannot upload exposure with null or empty id.");

        LOGGER.debug("Sending exposure '{}' to server...", id);
        Packets.sendToServer(new ExposureClientDataC2SP(id, exposure));
    }
}
