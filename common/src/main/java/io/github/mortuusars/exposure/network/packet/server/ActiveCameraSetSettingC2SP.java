package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.camera.CameraSetting;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ActiveCameraSetSettingC2SP(CameraSetting<?> setting, byte[] encodedValue) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("active_camera_set_setting");
    public static final Type<ActiveCameraSetSettingC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActiveCameraSetSettingC2SP> STREAM_CODEC = StreamCodec.composite(
            CameraSetting.STREAM_CODEC, ActiveCameraSetSettingC2SP::setting,
            ByteBufCodecs.byteArray(4096), ActiveCameraSetSettingC2SP::encodedValue,
            ActiveCameraSetSettingC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        if (player.getActiveExposureCamera() == null) return false;
        player.getActiveExposureCamera().ifPresent((item, stack) ->
                setting.decodeAndSet(player, stack, player.level().registryAccess(), encodedValue));
        return true;
    }
}