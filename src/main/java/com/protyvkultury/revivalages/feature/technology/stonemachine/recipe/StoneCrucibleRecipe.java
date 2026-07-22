package com.protyvkultury.revivalages.feature.technology.stonemachine.recipe;

import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
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
import net.neoforged.neoforge.fluids.FluidStack;

public final class StoneCrucibleRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final FluidStack result;
    private final int processingTime;

    public StoneCrucibleRecipe(Ingredient ingredient, FluidStack result, int processingTime) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.result = Objects.requireNonNull(result).copy();
        this.processingTime = Math.max(1, processingTime);
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public FluidStack result() {
        return result.copy();
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
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(ingredient);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StoneMachineFeature.STONE_CRUCIBLE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return StoneMachineFeature.STONE_CRUCIBLE_RECIPE_TYPE.get();
    }
}
