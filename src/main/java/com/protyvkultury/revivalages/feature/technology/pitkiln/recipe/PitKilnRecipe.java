package com.protyvkultury.revivalages.feature.technology.pitkiln.recipe;

import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import java.util.List;
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

public final class PitKilnRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int burnTime;
    private final float failureChance;
    private final List<ItemStack> failureResults;

    public PitKilnRecipe(
            Ingredient ingredient,
            ItemStack result,
            int burnTime,
            float failureChance,
            List<ItemStack> failureResults
    ) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.result = Objects.requireNonNull(result).copy();
        this.burnTime = Math.max(1, burnTime);
        this.failureChance = Math.clamp(failureChance, 0.0F, 1.0F);
        this.failureResults = failureResults.stream().map(ItemStack::copy).toList();
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result.copy();
    }

    public int burnTime() {
        return burnTime;
    }

    public float failureChance() {
        return failureChance;
    }

    public List<ItemStack> failureResults() {
        return failureResults.stream().map(ItemStack::copy).toList();
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
        return PitKilnFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PitKilnFeature.RECIPE_TYPE.get();
    }
}
