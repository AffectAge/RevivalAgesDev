package com.protyvkultury.revivalages.feature.technology.campfire.recipe;

import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class CampfireRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int cookingTime;

    public CampfireRecipe(Ingredient ingredient, ItemStack result, int cookingTime) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.result = Objects.requireNonNull(result).copy();
        this.cookingTime = Math.max(1, cookingTime);
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result.copy();
    }

    public int cookingTime() {
        return cookingTime;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(ingredient);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CampfireFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return CampfireFeature.RECIPE_TYPE.get();
    }
}
