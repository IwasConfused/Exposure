package io.github.mortuusars.exposure.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class FileLoadingInfo {
    public static final StreamCodec<FriendlyByteBuf, FileLoadingInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FileLoadingInfo::getFilepath,
            ByteBufCodecs.BOOL, FileLoadingInfo::shouldDither,
            FileLoadingInfo::new
    );

    private final String filepath;
    private final boolean dither;

    public FileLoadingInfo(String filepath, boolean dither) {
        this.filepath = filepath;
        this.dither = dither;
    }

    public String getFilepath() {
        return filepath;
    }

    public boolean shouldDither() {
        return dither;
    }
}
