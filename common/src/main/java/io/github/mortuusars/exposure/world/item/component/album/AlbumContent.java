package io.github.mortuusars.exposure.world.item.component.album;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record AlbumContent(List<AlbumPage> pages) {
    public static final Codec<AlbumContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AlbumPage.CODEC.sizeLimitedListOf(Album.MAX_PAGES).fieldOf("pages").forGetter(AlbumContent::pages)
    ).apply(instance, AlbumContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlbumContent> STREAM_CODEC = StreamCodec.composite(
            AlbumPage.STREAM_CODEC.apply(ByteBufCodecs.list(Album.MAX_PAGES)), AlbumContent::pages,
            AlbumContent::new
    );

    public static final AlbumContent EMPTY = new AlbumContent(Collections.emptyList());

    public AlbumContent {
        Preconditions.checkArgument(pages.size() <= Album.MAX_PAGES,
                "Too many pages for album. Max is " + Album.MAX_PAGES);
    }

    public boolean isEmpty() {
        return this.equals(EMPTY) || pages.stream().allMatch(AlbumPage::isEmpty);
    }

    public AlbumContent removeEmptyTrailingPages() {
        ArrayList<AlbumPage> pages = new ArrayList<>(this.pages);

        for (int i = pages.size() - 1; i >= 0; i--) {
            AlbumPage page = pages.get(i);

            if (page.isEmpty())
                pages.remove(i);
            else
                break;
        }

        return new AlbumContent(pages);
    }

    public static class Mutable {
        private final ArrayList<AlbumPage> pages;

        public Mutable(AlbumContent content) {
            NonNullList<AlbumPage> pages = NonNullList.withSize(Album.MAX_PAGES, AlbumPage.EMPTY);

            this.pages = new ArrayList<>(content.pages);
        }

        public ArrayList<AlbumPage> getPages() {
            return pages;
        }

        public Mutable setPage(int index, AlbumPage page) {
            Preconditions.checkElementIndex(index, Album.MAX_PAGES);
            pages.set(index, page);
            return this;
        }

        public AlbumContent toImmutable() {
            return new AlbumContent(pages);
        }
    }
}
