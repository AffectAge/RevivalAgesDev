package com.protyvkultury.revivalages.feature.technology.dryingrack.view;

import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class DryingRecipeCatalog {

    private DryingRecipeCatalog() {}

    public static List<DryingRecipeView> crude(RecipeManager recipeManager) {
        return sorted(recipeManager.getAllRecipesFor(DryingRackFeature.CRUDE_DRYING_RECIPE_TYPE.get()))
                .stream()
                .map(holder -> new DryingRecipeView(holder, holder.value().ingredient(), false))
                .toList();
    }

    public static List<DryingRecipeView> normal(RecipeManager recipeManager) {
        List<RecipeHolder<DryingRecipe>> own =
                sorted(recipeManager.getAllRecipesFor(DryingRackFeature.DRYING_RECIPE_TYPE.get()));
        List<DryingRecipeView> views = new ArrayList<>();
        for (RecipeHolder<DryingRecipe> holder : own) {
            views.add(new DryingRecipeView(holder, holder.value().ingredient(), false));
        }
        for (RecipeHolder<DryingRecipe> holder :
                sorted(recipeManager.getAllRecipesFor(DryingRackFeature.CRUDE_DRYING_RECIPE_TYPE.get()))) {
            Ingredient inheritedIngredient =
                    removeOverriddenAlternatives(holder.value().ingredient(), own);
            if (!inheritedIngredient.isEmpty()) {
                views.add(new DryingRecipeView(holder, inheritedIngredient, true));
            }
        }
        return List.copyOf(views);
    }

    private static Ingredient removeOverriddenAlternatives(
            Ingredient inherited, List<RecipeHolder<DryingRecipe>> ownRecipes) {
        ItemStack[] alternatives = inherited.getItems();
        if (alternatives.length == 0) {
            boolean exactOverride =
                    ownRecipes.stream()
                            .map(RecipeHolder::value)
                            .map(DryingRecipe::ingredient)
                            .anyMatch(inherited::equals);
            return exactOverride ? Ingredient.EMPTY : inherited;
        }
        ItemStack[] remaining =
                Arrays.stream(alternatives)
                        .filter(
                                stack ->
                                        ownRecipes.stream()
                                                .noneMatch(holder -> holder.value().ingredient().test(stack)))
                        .map(ItemStack::copy)
                        .toArray(ItemStack[]::new);
        return remaining.length == 0 ? Ingredient.EMPTY : Ingredient.of(remaining);
    }

    private static List<RecipeHolder<DryingRecipe>> sorted(List<RecipeHolder<DryingRecipe>> recipes) {
        return recipes.stream().sorted(Comparator.comparing(holder -> holder.id().toString())).toList();
    }
}
