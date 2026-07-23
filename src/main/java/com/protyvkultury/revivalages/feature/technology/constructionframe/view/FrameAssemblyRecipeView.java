package com.protyvkultury.revivalages.feature.technology.constructionframe.view;

import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameAssemblyRecipe;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Loader-neutral presentation model shared by all frame recipe viewers. */
public record FrameAssemblyRecipeView(
        ResourceLocation id,
        List<Ingredient> ingredients,
        Ingredient tool,
        ItemStack result,
        RecipeHolder<FrameAssemblyRecipe> backingRecipe
) {

    public FrameAssemblyRecipeView {
        ingredients = List.copyOf(ingredients);
        result = result.copy();
    }
}
