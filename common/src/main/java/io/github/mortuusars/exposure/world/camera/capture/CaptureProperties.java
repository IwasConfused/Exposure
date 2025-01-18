package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.world.camera.CameraID;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.entity.PhotographerEntity;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public record CaptureProperties(String exposureId,
                                Optional<ExposureType> filmType,
                                Optional<Holder<ColorPalette>> colorPalette,
                                Optional<Integer> frameSize,
                                Optional<Float> cropFactor,
                                Optional<ShutterSpeed> shutterSpeed,
                                Optional<Float> fovOverride,
                                boolean flash,
                                Optional<ProjectionInfo> projection,
                                Optional<ColorChannel> chromaticChannel,
                                Optional<UUID> photographerEntityId,
                                Optional<CameraID> cameraId,
                                CompoundTag extraData) {
    public static final Codec<CaptureProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(CaptureProperties::exposureId),
            ExposureType.CODEC.optionalFieldOf("film_type").forGetter(CaptureProperties::filmType),
            ColorPalette.HOLDER_CODEC.optionalFieldOf("color_palette").forGetter(CaptureProperties::colorPalette),
            Codec.INT.optionalFieldOf("frame_size").forGetter(CaptureProperties::frameSize),
            Codec.FLOAT.optionalFieldOf("crop_factor").forGetter(CaptureProperties::cropFactor),
            ShutterSpeed.CODEC.optionalFieldOf("shutter_speed").forGetter(CaptureProperties::shutterSpeed),
            Codec.FLOAT.optionalFieldOf("fov_override").forGetter(CaptureProperties::fovOverride),
            Codec.BOOL.optionalFieldOf("flash", false).forGetter(CaptureProperties::flash),
            ProjectionInfo.CODEC.optionalFieldOf("projection").forGetter(CaptureProperties::projection),
            ColorChannel.CODEC.optionalFieldOf("chromatic_channel").forGetter(CaptureProperties::chromaticChannel),
            UUIDUtil.LENIENT_CODEC.optionalFieldOf("photographer_id").forGetter(CaptureProperties::photographerEntityId),
            CameraID.CODEC.optionalFieldOf("camera_id").forGetter(CaptureProperties::cameraId),
            CompoundTag.CODEC.optionalFieldOf("extra_data", new CompoundTag()).forGetter(CaptureProperties::extraData)
    ).apply(instance, CaptureProperties::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CaptureProperties> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull CaptureProperties decode(RegistryFriendlyByteBuf buffer) {
            return new CaptureProperties(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.optional(ExposureType.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(ColorPalette.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.FLOAT).decode(buffer),
                    ByteBufCodecs.optional(ShutterSpeed.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.FLOAT).decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.optional(ProjectionInfo.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(ColorChannel.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(CameraID.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.COMPOUND_TAG.decode(buffer));
        }

        public void encode(RegistryFriendlyByteBuf buffer, CaptureProperties data) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, data.exposureId());
            ByteBufCodecs.optional(ExposureType.STREAM_CODEC).encode(buffer, data.filmType());
            ByteBufCodecs.optional(ColorPalette.STREAM_CODEC).encode(buffer, data.colorPalette());
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).encode(buffer, data.frameSize());
            ByteBufCodecs.optional(ByteBufCodecs.FLOAT).encode(buffer, data.cropFactor());
            ByteBufCodecs.optional(ShutterSpeed.STREAM_CODEC).encode(buffer, data.shutterSpeed());
            ByteBufCodecs.optional(ByteBufCodecs.FLOAT).encode(buffer, data.fovOverride());
            ByteBufCodecs.BOOL.encode(buffer, data.flash());
            ByteBufCodecs.optional(ProjectionInfo.STREAM_CODEC).encode(buffer, data.projection());
            ByteBufCodecs.optional(ColorChannel.STREAM_CODEC).encode(buffer, data.chromaticChannel());
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buffer, data.photographerEntityId());
            ByteBufCodecs.optional(CameraID.STREAM_CODEC).encode(buffer, data.cameraId());
            ByteBufCodecs.COMPOUND_TAG.encode(buffer, data.extraData());
        }
    };

    // --

    public static final String LIGHT_LEVEL = "light_level";

    // --

    public static final class Builder {
        private final String exposureId;

        private @Nullable ExposureType filmType = null;
        private @Nullable Holder<ColorPalette> colorPalette = null;
        private @Nullable Integer frameSize = null;
        private @Nullable Float cropFactor = null;
        private @Nullable ShutterSpeed shutterSpeed = null;
        private @Nullable Float fovOverride = null;
        private boolean flash = false;
        private @Nullable ProjectionInfo projectionInfo;
        private @Nullable ColorChannel chromaticChannel;
        private @Nullable UUID photographerEntityID = null;
        private @Nullable CameraID cameraID = null;
        private final CompoundTag extraData = new CompoundTag();

        public Builder(String exposureId) {
            this.exposureId = exposureId;
        }

        public Builder setFilmType(ExposureType filmType) {
            this.filmType = filmType;
            return this;
        }

        public Builder setColorPalette(Holder<ColorPalette> colorPalette) {
            this.colorPalette = colorPalette;
            return this;
        }

        public Builder setFrameSize(int frameSize) {
            this.frameSize = frameSize;
            return this;
        }

        public Builder setCropFactor(float cropFactor) {
            this.cropFactor = cropFactor;
            return this;
        }

        public Builder setShutterSpeed(ShutterSpeed shutterSpeed) {
            this.shutterSpeed = shutterSpeed;
            return this;
        }

        public Builder setFovOverride(@Nullable Float fovOverride) {
            this.fovOverride = fovOverride;
            return this;
        }

        public Builder setFlash(boolean flash) {
            this.flash = flash;
            return this;
        }

        public Builder setPhotographer(@Nullable PhotographerEntity photographer) {
            if (photographer == null) photographerEntityID = null;
            else photographerEntityID = photographer.asEntity().getUUID();
            return this;
        }

        public Builder setPhotographerId(@Nullable UUID photographerId) {
            photographerEntityID = photographerId;
            return this;
        }

        public Builder setCameraID(@Nullable CameraID cameraID) {
            this.cameraID = cameraID;
            return this;
        }

        public Builder setCameraID(Optional<CameraID> cameraID) {
            this.cameraID = cameraID.orElse(null);
            return this;
        }

        public Builder setProjectionInfo(@Nullable ProjectionInfo projectionInfo) {
            this.projectionInfo = projectionInfo;
            return this;
        }

        public Builder setProjectingInfo(Optional<ProjectionInfo> projectingInfo) {
            this.projectionInfo = projectingInfo.orElse(null);
            return this;
        }

        public Builder setChromaticChannel(@Nullable ColorChannel chromaticChannel) {
            this.chromaticChannel = chromaticChannel;
            return this;
        }

        public Builder setChromaticChannel(Optional<ColorChannel> chromaticChannel) {
            this.chromaticChannel = chromaticChannel.orElse(null);
            return this;
        }

        public Builder extraData(Consumer<CompoundTag> extraDataUpdater) {
            extraDataUpdater.accept(extraData);
            return this;
        }

        public CaptureProperties build() {
            return new CaptureProperties(exposureId,
                    Optional.ofNullable(this.filmType),
                    Optional.ofNullable(this.colorPalette),
                    Optional.ofNullable(this.frameSize),
                    Optional.ofNullable(this.cropFactor),
                    Optional.ofNullable(this.shutterSpeed),
                    Optional.ofNullable(this.fovOverride),
                    this.flash,
                    Optional.ofNullable(this.projectionInfo),
                    Optional.ofNullable(this.chromaticChannel),
                    Optional.ofNullable(this.photographerEntityID),
                    Optional.ofNullable(this.cameraID),
                    this.extraData);
        }
    }
}
