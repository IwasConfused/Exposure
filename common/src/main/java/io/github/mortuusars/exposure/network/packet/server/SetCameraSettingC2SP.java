package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.part.CameraSetting;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SetCameraSettingC2SP(CameraSetting<?> setting, byte[] encodedValue) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("set_camera_setting");
    public static final Type<SetCameraSettingC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SetCameraSettingC2SP> STREAM_CODEC = StreamCodec.composite(
            CameraSetting.STREAM_CODEC, SetCameraSettingC2SP::setting,
            ByteBufCodecs.byteArray(4096), SetCameraSettingC2SP::encodedValue,
            SetCameraSettingC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet {}: Player was null", ID);

        player.getActiveExposureCameraOptional().ifPresentOrElse(camera -> {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(encodedValue);
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(buf, player.level().registryAccess());
            setting.set(camera.getItemStack(), buffer);
            camera.ifPresent((item, stack) -> item.actionPerformed(stack, player.level()));
            buffer.clear();
        }, () -> Exposure.LOGGER.error("Cannot set camera setting '{}': player '{}' does not have active camera.", setting, player));

        return true;
    }
}