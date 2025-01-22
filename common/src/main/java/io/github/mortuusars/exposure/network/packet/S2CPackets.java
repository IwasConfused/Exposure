package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.client.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class S2CPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                new CustomPacketPayload.TypeAndCodec<>(ActiveCameraRemoveS2CP.TYPE, ActiveCameraRemoveS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ActiveCameraInHandSetS2CP.TYPE, ActiveCameraInHandSetS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ShaderApplyS2CP.TYPE, ShaderApplyS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ClearRenderingCacheS2CP.TYPE, ClearRenderingCacheS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(CreateChromaticExposureS2CP.TYPE, CreateChromaticExposureS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposureDataChangedS2CP.TYPE, ExposureDataChangedS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(UniqueSoundPlayS2CP.TYPE, UniqueSoundPlayS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(UniqueSoundPlayShutterTickingS2CP.TYPE, UniqueSoundPlayShutterTickingS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(UniqueSoundStopS2CP.TYPE, UniqueSoundStopS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ShowExposureCommandS2CP.TYPE, ShowExposureCommandS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposureDataResponseS2CP.TYPE, ExposureDataResponseS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(CaptureStartS2CP.TYPE, CaptureStartS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(CaptureStartDebugRGBS2CP.TYPE, CaptureStartDebugRGBS2CP.STREAM_CODEC)
        );
    }
}
