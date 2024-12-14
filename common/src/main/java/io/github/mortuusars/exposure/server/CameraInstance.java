package io.github.mortuusars.exposure.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import io.github.mortuusars.exposure.util.ByteArrayUtils;
import io.github.mortuusars.exposure.warehouse.ExposureData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

public class CameraInstance {
    public static final Codec<CameraInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("asd").forGetter(CameraInstance::getAsd)
    ).apply(instance, CameraInstance::new));

    private final int asd;

    public CameraInstance(int asd) {
        this.asd = asd;
    }

    public int getAsd() {
        return asd;
    }
}
