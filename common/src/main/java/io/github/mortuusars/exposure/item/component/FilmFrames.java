package io.github.mortuusars.exposure.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class FilmFrames {
//    public static final FilmFrames EMPTY = new FilmFrames(NonNullList.withSize(ChalkBoxItem.SLOTS, ItemStack.EMPTY), 0);
//
//    public static final Codec<FilmFrames> CODEC = RecordCodecBuilder.create(instance -> instance.group(
//                    ItemStack.OPTIONAL_CODEC.listOf()
//                            .optionalFieldOf("items", NonNullList.withSize(ChalkBoxItem.SLOTS, ItemStack.EMPTY))
//                            .forGetter(FilmFrames::items),
//                    Codec.INT
//                            .optionalFieldOf("glowing_uses", 0)
//                            .forGetter(FilmFrames::glowAmount))
//            .apply(instance, FilmFrames::new));
//
//    public static final StreamCodec<RegistryFriendlyByteBuf, FilmFrames> STREAM_CODEC = StreamCodec.composite(
//            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), FilmFrames::items,
//            ByteBufCodecs.INT, FilmFrames::glowAmount,
//            FilmFrames::new
//    );
}
