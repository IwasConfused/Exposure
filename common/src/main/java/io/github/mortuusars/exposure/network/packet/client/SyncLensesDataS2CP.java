package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.camera.FocalRange;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record SyncLensesDataS2CP(Map<Ingredient, FocalRange> lenses) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("sync_lenses_data");
    public static final CustomPacketPayload.Type<SyncLensesDataS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncLensesDataS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Ingredient.CONTENTS_STREAM_CODEC, FocalRange.STREAM_CODEC), SyncLensesDataS2CP::lenses,
            SyncLensesDataS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.syncLensesData(this);
        return true;
    }
}
