package io.github.mortuusars.exposure.item.component.album;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record AlbumPage(ItemStack photograph, String note) {
    public static final Codec<AlbumPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("photograph").forGetter(AlbumPage::photograph),
            Codec.string(0, 1024).optionalFieldOf("note", "").forGetter(AlbumPage::note)
    ).apply(instance, AlbumPage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlbumPage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, AlbumPage::photograph,
            ByteBufCodecs.STRING_UTF8, AlbumPage::note,
            AlbumPage::new
    );

    public static final AlbumPage EMPTY = new AlbumPage(ItemStack.EMPTY, "");

    public boolean isEmpty() {
        return this.equals(EMPTY) || (photograph().isEmpty() && note().isEmpty());
    }

    public SignedAlbumPage convertToSigned() {
        return new SignedAlbumPage(photograph, Component.literal(note));
    }
}
