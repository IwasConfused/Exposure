package io.github.mortuusars.exposure.client.capture.saving;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.warehouse.ExposureData;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ExposureClientDataC2SP;
import net.minecraft.util.StringUtil;

public class PalettedExposureUploader {
    public static void upload(String id, ExposureData exposure) {
        Preconditions.checkArgument(!StringUtil.isBlank(id), "Cannot upload exposure with null or empty id.");

        Exposure.LOGGER.debug("Sending exposure '{}' to server...", id);
        Packets.sendToServer(new ExposureClientDataC2SP(id, exposure));
    }
}
