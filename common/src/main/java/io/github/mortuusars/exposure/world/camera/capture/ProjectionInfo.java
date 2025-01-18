package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ProjectionInfo(String path, ProjectionMode mode) {
    public static final Codec<ProjectionInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("path").forGetter(ProjectionInfo::path),
            ProjectionMode.CODEC.optionalFieldOf("mode", ProjectionMode.DITHERED).forGetter(ProjectionInfo::mode)
    ).apply(instance, ProjectionInfo::new));

    public static final StreamCodec<FriendlyByteBuf, ProjectionInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ProjectionInfo::path,
            ProjectionMode.STREAM_CODEC, ProjectionInfo::mode,
            ProjectionInfo::new
    );
}
