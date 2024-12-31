package io.github.mortuusars.exposure.data.filter;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.core.color.Color;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class Filter {
    public static final ResourceLocation DEFAULT_GLASS_TEXTURE = Exposure.resource("textures/gui/filter/stained_glass.png");
    public static final Color DEFAULT_TINT_COLOR = Color.WHITE;

    private final Ingredient ingredient;
    private final ResourceLocation shader;
    private final ResourceLocation attachmentTexture;
    private final Color tintColor;

    public Filter(Ingredient ingredient, ResourceLocation shader, ResourceLocation attachmentTexture, Color uiTintColor) {
        this.ingredient = ingredient;
        this.shader = shader;
        this.attachmentTexture = attachmentTexture;
        this.tintColor = uiTintColor;
    }

    public boolean matches(ItemStack stack) {
        return ingredient.test(stack);
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ResourceLocation getShader() {
        return shader;
    }

    public ResourceLocation getAttachmentTexture() {
        return attachmentTexture;
    }

    public Color getTintColor() {
        return tintColor;
    }
}
