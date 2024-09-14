package io.github.mortuusars.exposure.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FilmDevelopingRecipe extends AbstractComponentTransferringRecipe {
    public FilmDevelopingRecipe(Ingredient filmIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(filmIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.FILM_DEVELOPING.get();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);

        for (int i = 0; i < input.size(); ++i) {
            ItemStack item = input.getItem(i);
            if (item.getItem() instanceof PotionItem) {
                remainingItems.set(i, new ItemStack(Items.GLASS_BOTTLE));
            } else if (item.getItem().hasCraftingRemainingItem()) {
                remainingItems.set(i, new ItemStack(Objects.requireNonNull(item.getItem().getCraftingRemainingItem())));
            }
        }

        return remainingItems;
    }

    public static class Serializer implements RecipeSerializer<FilmDevelopingRecipe> {
        public static final MapCodec<FilmDevelopingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("film").forGetter(FilmDevelopingRecipe::getSourceIngredient),
                Ingredient.CODEC_NONEMPTY
                        .listOf()
                        .fieldOf("ingredients")
                        .flatXmap(
                                list -> {
                                    Ingredient[] ingredients = list.toArray(Ingredient[]::new);
                                    if (ingredients.length == 0) {
                                        return DataResult.error(() -> "No ingredients for film developing recipe");
                                    } else {
                                        return ingredients.length > 9
                                                ? DataResult.error(() -> ("Too many ingredients for film developing recipe. " +
                                                "The maximum is: %s").formatted(9))
                                                : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
                                    }
                                },
                                DataResult::success
                        )
                        .forGetter(FilmDevelopingRecipe::getIngredients),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(FilmDevelopingRecipe::getResult)
        ).apply(instance, FilmDevelopingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FilmDevelopingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, FilmDevelopingRecipe::getSourceIngredient,
                        ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC), FilmDevelopingRecipe::getIngredients,
                        ItemStack.STREAM_CODEC, FilmDevelopingRecipe::getResult,
                        FilmDevelopingRecipe::new
                );

        @Override
        public @NotNull MapCodec<FilmDevelopingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, FilmDevelopingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
