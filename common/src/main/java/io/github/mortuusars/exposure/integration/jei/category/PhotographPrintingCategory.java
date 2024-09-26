package io.github.mortuusars.exposure.integration.jei.category;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.ExposureType;
import io.github.mortuusars.exposure.integration.jei.ExposureJeiPlugin;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographPrintingJeiRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PhotographPrintingCategory implements IRecipeCategory<PhotographPrintingJeiRecipe> {
    private static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/jei/photograph_printing.png");
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic filmDrawable;

    public PhotographPrintingCategory(IGuiHelper guiHelper) {
        title = Component.translatable("jei.exposure.photograph_printing.title");
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 170, 80);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Exposure.Items.LIGHTROOM.get()));

        filmDrawable = guiHelper.createDrawable(TEXTURE, 0, 80, 170, 80);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, @NotNull PhotographPrintingJeiRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 78, 17)
                .addItemStack(new ItemStack(recipe.getExposureType() == ExposureType.COLOR ?
                        Exposure.Items.DEVELOPED_COLOR_FILM.get()
                        : Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get()))
                .setSlotName("Film");


        List<ItemStack> papers = BuiltInRegistries.ITEM.getTag(Exposure.Tags.Items.PHOTO_PAPERS)
                .map(holders -> holders.stream()
                        .map(itemHolder -> new ItemStack(itemHolder.value())).collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        builder.addSlot(RecipeIngredientRole.CATALYST, 6, 55)
                .addItemStacks(papers)
                .setSlotName("Paper");

        if (recipe.getExposureType() == ExposureType.COLOR) {
            List<ItemStack> cyanDyes = BuiltInRegistries.ITEM.getTag(Exposure.Tags.Items.CYAN_PRINTING_DYES)
                    .map(holders -> holders.stream()
                            .map(itemHolder -> new ItemStack(itemHolder.value())).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());

            builder.addSlot(RecipeIngredientRole.CATALYST, 40, 55)
                    .addItemStacks(cyanDyes)
                    .setSlotName("Cyan");

            List<ItemStack> magentaDyes = BuiltInRegistries.ITEM.getTag(Exposure.Tags.Items.MAGENTA_PRINTING_DYES)
                    .map(holders -> holders.stream()
                            .map(itemHolder -> new ItemStack(itemHolder.value())).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());

            builder.addSlot(RecipeIngredientRole.CATALYST, 58, 55)
                    .addItemStacks(magentaDyes)
                    .setSlotName("Magenta");

            List<ItemStack> yellowDyes = BuiltInRegistries.ITEM.getTag(Exposure.Tags.Items.YELLOW_PRINTING_DYES)
                    .map(holders -> holders.stream()
                            .map(itemHolder -> new ItemStack(itemHolder.value())).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());

            builder.addSlot(RecipeIngredientRole.CATALYST, 76, 55)
                    .addItemStacks(yellowDyes)
                    .setSlotName("Yellow");
        }

        List<ItemStack> blackDyes = BuiltInRegistries.ITEM.getTag(Exposure.Tags.Items.BLACK_PRINTING_DYES)
                .map(holders -> holders.stream()
                        .map(itemHolder -> new ItemStack(itemHolder.value())).collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        builder.addSlot(RecipeIngredientRole.CATALYST, 94, 55)
                .addItemStacks(blackDyes)
                .setSlotName("Black");

        ItemStack resultItemStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
        resultItemStack.set(Exposure.DataComponents.PHOTOGRAPH_TYPE, recipe.getExposureType());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 144, 55).addItemStack(resultItemStack);
    }

    @Override
    public void draw(PhotographPrintingJeiRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe.getExposureType() == ExposureType.COLOR)
            RenderSystem.setShaderColor(1.1F, 0.86F, 0.66F, 1.0F);
        filmDrawable.draw(guiGraphics);
    }

    @Override
    public @NotNull RecipeType<PhotographPrintingJeiRecipe> getRecipeType() {
        return ExposureJeiPlugin.PHOTOGRAPH_PRINTING_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }
}