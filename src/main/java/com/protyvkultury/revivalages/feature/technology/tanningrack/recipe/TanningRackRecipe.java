package com.protyvkultury.revivalages.feature.technology.tanningrack.recipe;

import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
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

public final class TanningRackRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final ItemStack rainFailure;
    private final int processingTime;

    public TanningRackRecipe(Ingredient ingredient, ItemStack result, ItemStack rainFailure, int processingTime) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.result = Objects.requireNonNull(result).copy();
        this.rainFailure = Objects.requireNonNull(rainFailure).copy();
        this.processingTime = Math.max(1, processingTime);
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result.copy();
    }

    public ItemStack rainFailure() {
        return rainFailure.copy();
    }

    public int processingTime() {
        return processingTime;
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
        return TanningRackFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TanningRackFeature.RECIPE_TYPE.get();
    }
}
