package com.protyvkultury.revivalages.feature.technology.dryingrack.view;

import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipe;
import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipeDuration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public record DryingRecipeView(
        RecipeHolder<DryingRecipe> holder,
        Ingredient displayIngredient,
        boolean normalRack,
        boolean inherited) {
    public ResourceLocation id() {
        return this.holder.id();
    }

    public DryingRecipe recipe() {
        return (DryingRecipe) this.holder.value();
    }

    public int processingTime() {
        return DryingRecipeDuration.effective(recipe().dryingTime(), normalRack, inherited);
    }
}
