package io.github.mortuusars.exposure.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PhotographCopyingRecipe extends AbstractComponentTransferringRecipe {
    public PhotographCopyingRecipe(Ingredient sourceIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(sourceIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.PHOTOGRAPH_CLONING.get();
    }

    @Override
    public @NotNull ItemStack transferComponents(ItemStack photographStack, ItemStack recipeResultStack) {
        Integer generation = photographStack.get(Exposure.DataComponents.PHOTOGRAPH_GENERATION);
        if (photographStack.getItem() instanceof PhotographItem
                && (generation == null || generation < 2)) {
            ItemStack result = super.transferComponents(photographStack, recipeResultStack);
            int gen = generation != null ? generation + 1 : 1;
            result.set(Exposure.DataComponents.PHOTOGRAPH_GENERATION, gen);
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem().hasCraftingRemainingItem()) {
                nonnulllist.set(i, new ItemStack(Objects.requireNonNull(stack.getItem().getCraftingRemainingItem())));
            } else if (stack.getItem() instanceof PhotographItem) {
                ItemStack remainingPhotographStack = stack.copy();
                remainingPhotographStack.setCount(1);
                nonnulllist.set(i, remainingPhotographStack);
            }
        }

        return nonnulllist;
    }

    public static class Serializer implements RecipeSerializer<PhotographCopyingRecipe> {
        public static final MapCodec<PhotographCopyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("photograph").forGetter(PhotographCopyingRecipe::getSourceIngredient),
                Ingredient.CODEC_NONEMPTY
                        .listOf()
                        .fieldOf("ingredients")
                        .flatXmap(
                                list -> {
                                    Ingredient[] ingredients = list.toArray(Ingredient[]::new);
                                    if (ingredients.length == 0) {
                                        return DataResult.error(() -> "No ingredients for photograph copying recipe");
                                    } else {
                                        return ingredients.length > 9
                                                ? DataResult.error(() -> ("Too many ingredients for photograph copying recipe. " +
                                                "The maximum is: %s").formatted(9))
                                                : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
                                    }
                                },
                                DataResult::success
                        )
                        .forGetter(PhotographCopyingRecipe::getIngredients),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(PhotographCopyingRecipe::getResult)
        ).apply(instance, PhotographCopyingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PhotographCopyingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, PhotographCopyingRecipe::getSourceIngredient,
                        ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC), PhotographCopyingRecipe::getIngredients,
                        ItemStack.STREAM_CODEC, PhotographCopyingRecipe::getResult,
                        PhotographCopyingRecipe::new
                );

        @Override
        public @NotNull MapCodec<PhotographCopyingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, PhotographCopyingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
