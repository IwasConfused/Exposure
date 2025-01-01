package io.github.mortuusars.exposure.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class FileProjectingInfo {
    public static final StreamCodec<FriendlyByteBuf, FileProjectingInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FileProjectingInfo::getFilepath,
            ByteBufCodecs.BOOL, FileProjectingInfo::shouldDither,
            FileProjectingInfo::new
    );

    private final String filepath;
    private final boolean dither;

    public FileProjectingInfo(String filepath, boolean dither) {
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
