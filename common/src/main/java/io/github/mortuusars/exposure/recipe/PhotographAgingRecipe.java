package io.github.mortuusars.exposure.recipe;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class PhotographAgingRecipe extends ComponentTransferringRecipe {
    public PhotographAgingRecipe(CraftingBookCategory category, Ingredient sourceIngredient,
                                 NonNullList<Ingredient> ingredients, ItemStack result) {
        super(category, sourceIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.PHOTOGRAPH_AGING.get();
    }
}
