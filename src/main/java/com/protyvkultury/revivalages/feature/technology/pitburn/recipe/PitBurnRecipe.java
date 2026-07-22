package com.protyvkultury.revivalages.feature.technology.pitburn.recipe;

import com.protyvkultury.revivalages.feature.technology.pitburn.PitBurnFeature;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** Data-driven recipe for one pile in an enclosed pit burn. */
public record PitBurnRecipe(
        Ingredient ingredient,
        ItemStack result,
        int stages,
        int burnTime,
        float failureChance,
        List<ItemStack> failureResults
) implements Recipe<SingleRecipeInput> {

    public PitBurnRecipe {
        result = result.copy();
        stages = Math.max(1, stages);
        burnTime = Math.max(1, burnTime);
        failureChance = Math.clamp(failureChance, 0.0F, 1.0F);
        failureResults = failureResults.stream().map(ItemStack::copy).toList();
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
        return PitBurnFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PitBurnFeature.RECIPE_TYPE.get();
    }
}
