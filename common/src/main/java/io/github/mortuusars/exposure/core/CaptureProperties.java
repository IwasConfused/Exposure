package io.github.mortuusars.exposure.core;

import io.github.mortuusars.exposure.core.camera.CameraID;
import io.github.mortuusars.exposure.core.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.core.color.ChromaChannel;
import io.github.mortuusars.exposure.core.color.ColorPalette;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record CaptureProperties(String exposureID,
                                UUID photographerEntityID,
                                Optional<CameraID> cameraID,
                                ShutterSpeed shutterSpeed,
                                Optional<Float> fovOverride,
                                ExposureType filmType,
                                int frameSize,
                                float cropFactor,
                                ColorPalette colorPalette,
                                boolean flash,
                                int lightLevel,
                                Optional<FileLoadingInfo> fileLoadingInfo,
                                Optional<ChromaChannel> chromaChannel,
                                CompoundTag extraData) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CaptureProperties> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull CaptureProperties decode(RegistryFriendlyByteBuf buffer) {
            return new CaptureProperties(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    UUIDUtil.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.optional(CameraID.STREAM_CODEC).decode(buffer),
                    ShutterSpeed.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.FLOAT).decode(buffer),
                    ExposureType.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ColorPalette.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.optional(FileLoadingInfo.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(ChromaChannel.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.COMPOUND_TAG.decode(buffer));
        }

        public void encode(RegistryFriendlyByteBuf buffer, CaptureProperties data) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, data.exposureID());
            UUIDUtil.STREAM_CODEC.encode(buffer, data.photographerEntityID());
            ByteBufCodecs.optional(CameraID.STREAM_CODEC).encode(buffer, data.cameraID());
            ShutterSpeed.STREAM_CODEC.encode(buffer, data.shutterSpeed());
            ByteBufCodecs.optional(ByteBufCodecs.FLOAT).encode(buffer, data.fovOverride());
            ExposureType.STREAM_CODEC.encode(buffer, data.filmType());
            ByteBufCodecs.VAR_INT.encode(buffer, data.frameSize());
            ByteBufCodecs.FLOAT.encode(buffer, data.cropFactor());
            ColorPalette.STREAM_CODEC.encode(buffer, data.colorPalette());
            ByteBufCodecs.BOOL.encode(buffer, data.flash());
            ByteBufCodecs.VAR_INT.encode(buffer, data.lightLevel());
            ByteBufCodecs.optional(FileLoadingInfo.STREAM_CODEC).encode(buffer, data.fileLoadingInfo());
            ByteBufCodecs.optional(ChromaChannel.STREAM_CODEC).encode(buffer, data.chromaChannel());
            ByteBufCodecs.COMPOUND_TAG.encode(buffer, data.extraData());
        }
    };
}
