package com.protyvkultury.revivalages.feature.technology.stonemachine.recipe;

import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
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

public final class StoneKilnRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int processingTime;
    private final float failureChance;
    private final List<ItemStack> failureResults;

    public StoneKilnRecipe(
            Ingredient ingredient,
            ItemStack result,
            int processingTime,
            float failureChance,
            List<ItemStack> failureResults
    ) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.result = Objects.requireNonNull(result).copy();
        this.processingTime = Math.max(1, processingTime);
        this.failureChance = Math.clamp(failureChance, 0.0F, 1.0F);
        this.failureResults = failureResults.stream().map(ItemStack::copy).toList();
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result.copy();
    }

    public int processingTime() {
        return processingTime;
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
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(ingredient);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StoneMachineFeature.STONE_KILN_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StoneMachineFeature.STONE_KILN_RECIPE_TYPE.get();
    }
}
