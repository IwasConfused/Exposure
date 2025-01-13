package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import io.github.mortuusars.exposure.world.inventory.AlbumMenu;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record AlbumSyncNoteC2SP(int pageIndex, String text) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("album_update_note");
    public static final CustomPacketPayload.Type<AlbumSyncNoteC2SP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, AlbumSyncNoteC2SP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AlbumSyncNoteC2SP::pageIndex,
            ByteBufCodecs.STRING_UTF8, AlbumSyncNoteC2SP::text,
            AlbumSyncNoteC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        if (!(player.containerMenu instanceof AlbumMenu albumMenu))
            throw new IllegalStateException("Player receiving this packet should have AlbumMenu open. Current menu: " + player.containerMenu);

        AlbumPage page = albumMenu.getPages().get(pageIndex);
        albumMenu.getPages().set(pageIndex, new AlbumPage(page.photograph(), text));

        albumMenu.updateAlbumStack();

        return true;
    }
}
