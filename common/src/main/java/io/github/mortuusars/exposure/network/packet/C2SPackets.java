package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.server.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class C2SPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                new CustomPacketPayload.TypeAndCodec<>(AlbumSignC2SP.TYPE, AlbumSignC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(AlbumSyncNoteC2SP.TYPE, AlbumSyncNoteC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(CameraSetSettingC2SP.TYPE, CameraSetSettingC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposureDataPartC2SP.TYPE, ExposureDataPartC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(OpenCameraAttachmentsInCreativePacketC2SP.TYPE, OpenCameraAttachmentsInCreativePacketC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(QueryExposureDataC2SP.TYPE, QueryExposureDataC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ActiveCameraReleaseC2SP.TYPE, ActiveCameraReleaseC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ActiveCameraAddFrameC2SP.TYPE, ActiveCameraAddFrameC2SP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(InterplanarProjectionFinishedC2SP.TYPE, InterplanarProjectionFinishedC2SP.STREAM_CODEC)
        );
    }
}
