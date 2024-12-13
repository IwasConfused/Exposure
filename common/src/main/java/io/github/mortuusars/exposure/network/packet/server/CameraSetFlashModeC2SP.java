package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.CameraAccessor;
import io.github.mortuusars.exposure.core.camera.component.FlashMode;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record CameraSetFlashModeC2SP(CameraAccessor accessor, FlashMode flashMode) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("camera_set_flash_mode");
    public static final CustomPacketPayload.Type<CameraSetFlashModeC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CameraSetFlashModeC2SP> STREAM_CODEC = StreamCodec.composite(
            CameraAccessor.STREAM_CODEC, CameraSetFlashModeC2SP::accessor,
            FlashMode.STREAM_CODEC, CameraSetFlashModeC2SP::flashMode,
            CameraSetFlashModeC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet {}: Player was null", ID);
        accessor.getCamera(player)
                .ifPresent(camera -> camera.getItem().setFlashMode(camera.getItemStack(), flashMode));
        return true;
    }
}
