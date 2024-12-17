package io.github.mortuusars.exposure.network.packet.server;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record OpenCameraAttachmentsInCreativePacketC2SP(int cameraSlotIndex) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("open_camera_attachments");
    public static final CustomPacketPayload.Type<OpenCameraAttachmentsInCreativePacketC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenCameraAttachmentsInCreativePacketC2SP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenCameraAttachmentsInCreativePacketC2SP::cameraSlotIndex,
            OpenCameraAttachmentsInCreativePacketC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.server.execute(() -> {
                ItemStack stack = player.getInventory().getItem(cameraSlotIndex);
                if (stack.getItem() instanceof CameraItem cameraItem)
                    cameraItem.openCameraAttachments(player, cameraSlotIndex);
            });
        }

        return true;
    }
}
