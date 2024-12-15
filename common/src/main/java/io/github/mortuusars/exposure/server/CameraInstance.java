package io.github.mortuusars.exposure.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
