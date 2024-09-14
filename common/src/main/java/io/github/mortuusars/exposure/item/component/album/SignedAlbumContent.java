
package io.github.mortuusars.exposure.item.component.album;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.item.AlbumItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SignedAlbumContent(String title, String author, List<SignedAlbumPage> pages) {
    public static final Codec<SignedAlbumContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("title").forGetter(SignedAlbumContent::title),
            Codec.STRING.fieldOf("author").forGetter(SignedAlbumContent::author),
            SignedAlbumPage.CODEC.sizeLimitedListOf(Album.MAX_PAGES).fieldOf("pages").forGetter(SignedAlbumContent::pages)
    ).apply(instance, SignedAlbumContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SignedAlbumContent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SignedAlbumContent::title,
            ByteBufCodecs.STRING_UTF8, SignedAlbumContent::author,
            SignedAlbumPage.STREAM_CODEC.apply(ByteBufCodecs.list(Album.MAX_PAGES)), SignedAlbumContent::pages,
            SignedAlbumContent::new
    );

    public static final SignedAlbumContent EMPTY = new SignedAlbumContent("", "", Collections.emptyList());

    public SignedAlbumContent {
        Preconditions.checkArgument(pages.size() <= Album.MAX_PAGES,
                "Too many pages for signed album. Max is " + Album.MAX_PAGES);
    }

//    public boolean isEmpty() {
//        return this.equals(EMPTY) || pages.stream().allMatch(SignedAlbumPage::isEmpty);
//    }

//    public static class Mutable {
//        private final ArrayList<SignedAlbumPage> pages;
//
//        public Mutable(SignedAlbumContent content) {
//            NonNullList<SignedAlbumPage> pages = NonNullList.withSize(Album.MAX_PAGES, SignedAlbumPage.EMPTY);
//
//            this.pages = new ArrayList<>(content.pages);
//        }
//
//        public ArrayList<SignedAlbumPage> getPages() {
//            return pages;
//        }
//
//        public Mutable setPage(int index, SignedAlbumPage page) {
//            Preconditions.checkElementIndex(index, Album.MAX_PAGES);
//            pages.set(index, page);
//            return this;
//        }
//
//        public SignedAlbumContent toImmutable() {
//            return new SignedAlbumContent(pages);
//        }
//    }
}
