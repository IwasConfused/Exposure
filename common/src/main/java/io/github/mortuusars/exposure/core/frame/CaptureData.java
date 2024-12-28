package io.github.mortuusars.exposure.core.frame;

import io.github.mortuusars.exposure.core.ChromaChannel;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.PhotographerEntity;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.core.image.color.ColorPalette;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record CaptureData(String id,
                          PhotographerEntity photographer,
                          CameraID cameraID,
                          ShutterSpeed shutterSpeed,
                          Optional<Integer> focalLengthOverride,
                          ExposureType filmType,
                          int frameSize,
                          float cropFactor,
                          ColorPalette colorPalette,
                          boolean flashHasFired,
                          int lightLevel,
                          Optional<FileProjectingInfo> fileProjectingInfo,
                          Optional<ChromaChannel> chromaChannel,
                          CompoundTag extraData) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CaptureData> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull CaptureData decode(RegistryFriendlyByteBuf buffer) {
            return new CaptureData(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    PhotographerEntity.STREAM_CODEC.decode(buffer),
                    CameraID.STREAM_CODEC.decode(buffer),
                    ShutterSpeed.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).decode(buffer),
                    ExposureType.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ColorPalette.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.optional(FileProjectingInfo.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(ChromaChannel.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.COMPOUND_TAG.decode(buffer));
        }

        public void encode(RegistryFriendlyByteBuf buffer, CaptureData data) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, data.id());
            PhotographerEntity.STREAM_CODEC.encode(buffer, data.photographer());
            CameraID.STREAM_CODEC.encode(buffer, data.cameraID());
            ShutterSpeed.STREAM_CODEC.encode(buffer, data.shutterSpeed());
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).encode(buffer, data.focalLengthOverride());
            ExposureType.STREAM_CODEC.encode(buffer, data.filmType());
            ByteBufCodecs.VAR_INT.encode(buffer, data.frameSize());
            ByteBufCodecs.FLOAT.encode(buffer, data.cropFactor());
            ColorPalette.STREAM_CODEC.encode(buffer, data.colorPalette());
            ByteBufCodecs.BOOL.encode(buffer, data.flashHasFired());
            ByteBufCodecs.VAR_INT.encode(buffer, data.lightLevel());
            ByteBufCodecs.optional(FileProjectingInfo.STREAM_CODEC).encode(buffer, data.fileProjectingInfo());
            ByteBufCodecs.optional(ChromaChannel.STREAM_CODEC).encode(buffer, data.chromaChannel());
            ByteBufCodecs.COMPOUND_TAG.encode(buffer, data.extraData());
        }
    };
}
