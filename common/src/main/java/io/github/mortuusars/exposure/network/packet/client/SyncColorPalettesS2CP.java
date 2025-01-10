package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.color.ColorPalette;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record SyncColorPalettesS2CP(Map<ResourceLocation, ColorPalette> palettes) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("sync_color_palettes");
    public static final Type<SyncColorPalettesS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncColorPalettesS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ColorPalette.DIRECT_STREAM_CODEC), SyncColorPalettesS2CP::palettes,
            SyncColorPalettesS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
//        ClientPacketsHandler.syncColorPalettes(this);
        return true;
    }
}
