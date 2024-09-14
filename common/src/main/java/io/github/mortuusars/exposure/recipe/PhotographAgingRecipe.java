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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class PhotographAgingRecipe extends AbstractComponentTransferringRecipe {
    public PhotographAgingRecipe(Ingredient transferIngredient,
                                 NonNullList<Ingredient> ingredients, ItemStack result) {
        super(transferIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.PHOTOGRAPH_AGING.get();
    }

    public static class Serializer implements RecipeSerializer<PhotographAgingRecipe> {
        public static final MapCodec<PhotographAgingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("photograph").forGetter(PhotographAgingRecipe::getSourceIngredient),
                Ingredient.CODEC_NONEMPTY
                        .listOf()
                        .fieldOf("ingredients")
                        .flatXmap(
                                list -> {
                                    Ingredient[] ingredients = list.toArray(Ingredient[]::new);
                                    if (ingredients.length == 0) {
                                        return DataResult.error(() -> "No ingredients for photograph aging recipe");
                                    } else {
                                        return ingredients.length > 9
                                                ? DataResult.error(() -> ("Too many ingredients for photograph aging recipe. " +
                                                "The maximum is: %s").formatted(9))
                                                : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
                                    }
                                },
                                DataResult::success
                        )
                        .forGetter(PhotographAgingRecipe::getIngredients),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(PhotographAgingRecipe::getResult)
        ).apply(instance, PhotographAgingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PhotographAgingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, PhotographAgingRecipe::getSourceIngredient,
                        ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC), PhotographAgingRecipe::getIngredients,
                        ItemStack.STREAM_CODEC, PhotographAgingRecipe::getResult,
                        PhotographAgingRecipe::new
                );

        @Override
        public @NotNull MapCodec<PhotographAgingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, PhotographAgingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
