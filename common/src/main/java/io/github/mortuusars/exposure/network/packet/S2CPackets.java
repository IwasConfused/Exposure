package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.client.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public class S2CPackets {
    public static List<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
        return List.of(
                new CustomPacketPayload.TypeAndCodec<>(ApplyShaderS2CP.TYPE, ApplyShaderS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ClearRenderingCacheS2CP.TYPE, ClearRenderingCacheS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(CreateChromaticExposureS2CP.TYPE, CreateChromaticExposureS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposeCommandS2CP.TYPE, ExposeCommandS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposureDataChangedS2CP.TYPE, ExposureDataChangedS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposureDataPartS2CP.TYPE, ExposureDataPartS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ExposureDataS2CP.TYPE, ExposureDataS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(LoadExposureFromFileCommandS2CP.TYPE, LoadExposureFromFileCommandS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(PlayOnePerEntitySoundS2CP.TYPE, PlayOnePerEntitySoundS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(PlayOnePerEntityShutterTickingSoundS2CP.TYPE, PlayOnePerEntityShutterTickingSoundS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(ShowExposureCommandS2CP.TYPE, ShowExposureCommandS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(StopOnePerEntitySoundS2CP.TYPE, StopOnePerEntitySoundS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(SyncLensesDataS2CP.TYPE, SyncLensesDataS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(WaitForExposureChangeS2CP.TYPE, WaitForExposureChangeS2CP.STREAM_CODEC),

                new CustomPacketPayload.TypeAndCodec<>(ExposureDataResponseS2CP.TYPE, ExposureDataResponseS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(RemoveActiveCameraS2CP.TYPE, RemoveActiveCameraS2CP.STREAM_CODEC),
                new CustomPacketPayload.TypeAndCodec<>(StartCaptureS2CP.TYPE, StartCaptureS2CP.STREAM_CODEC)
        );
    }
}
