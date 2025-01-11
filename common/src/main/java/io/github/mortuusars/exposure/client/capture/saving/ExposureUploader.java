package io.github.mortuusars.exposure.client.capture.saving;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.core.warehouse.ExposureData;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.ExposureDataC2SP;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

import java.util.function.Consumer;

public class ExposureUploader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void upload(String id, ExposureData exposure) {
        Preconditions.checkArgument(!StringUtil.isBlank(id), "Cannot upload exposure with null or empty id.");

        LOGGER.debug("Sending exposure '{}' to server...", id);
        Packets.sendToServer(new ExposureDataC2SP(id, exposure));
    }

    public static Consumer<ExposureData> upload(String id) {
        return exposure -> upload(id, exposure);
    }
}
