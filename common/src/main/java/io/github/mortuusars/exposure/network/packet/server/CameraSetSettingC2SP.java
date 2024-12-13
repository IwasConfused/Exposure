package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.item.part.Setting;
import io.github.mortuusars.exposure.network.packet.IPacket;
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

//public record CameraSetSettingC2SP(CameraAccessor accessor, Setting<?> setting, byte[] encodedValue) implements IPacket {
//    public static final ResourceLocation ID = Exposure.resource("camera_set_setting");
//    public static final Type<CameraSetSettingC2SP> TYPE = new Type<>(ID);
//
//    public static final StreamCodec<FriendlyByteBuf, CameraSetSettingC2SP> STREAM_CODEC = StreamCodec.composite(
//            CameraAccessor.STREAM_CODEC, CameraSetSettingC2SP::accessor,
//            Setting.STREAM_CODEC, CameraSetSettingC2SP::setting,
//            ByteBufCodecs.byteArray(4096), CameraSetSettingC2SP::encodedValue,
//            CameraSetSettingC2SP::new
//    );
//
//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
//
//    @Override
//    public boolean handle(PacketFlow direction, Player player) {
//        Preconditions.checkState(player != null, "Cannot handle packet {}: Player was null", ID);
//
//        accessor.getCamera(player)
//                .ifPresent(camera -> {
//                    RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.level().registryAccess());
//                    buffer.writeByteArray(encodedValue);
//                    setting.set(camera.getItemStack(), buffer);
//                    buffer.clear();
//                });
//        return true;
//    }
//}

public record CameraSetSettingC2SP(CameraAccessor accessor, Setting<?> setting, byte[] encodedValue) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("camera_set_setting");
    public static final Type<CameraSetSettingC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CameraSetSettingC2SP> STREAM_CODEC = StreamCodec.composite(
            CameraAccessor.STREAM_CODEC, CameraSetSettingC2SP::accessor,
            Setting.STREAM_CODEC, CameraSetSettingC2SP::setting,
            ByteBufCodecs.byteArray(4096), CameraSetSettingC2SP::encodedValue,
            CameraSetSettingC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet {}: Player was null", ID);

        accessor.getCamera(player)
                .ifPresent(camera -> {
                    ByteBuf buf = Unpooled.buffer();
                    buf.writeBytes(encodedValue);
                    RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(buf, player.level().registryAccess());
                    setting.set(camera.getItemStack(), buffer);
                    buffer.clear();
                });
        return true;
    }
}