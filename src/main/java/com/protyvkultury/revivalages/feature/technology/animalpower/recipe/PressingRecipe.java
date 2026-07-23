package com.protyvkultury.revivalages.feature.technology.animalpower.recipe;

import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
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

public final class PressingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final int inputCount;
    private final ItemStack itemResult;
    private final FluidStack fluidResult;

    public PressingRecipe(Ingredient ingredient, int inputCount, ItemStack itemResult, FluidStack fluidResult) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.inputCount = inputCount;
        this.itemResult = Objects.requireNonNull(itemResult).copy();
        this.fluidResult = Objects.requireNonNull(fluidResult).copy();
        if (inputCount <= 0) {
            throw new IllegalArgumentException("input_count must be positive");
        }
        if (itemResult.isEmpty() == fluidResult.isEmpty()) {
            throw new IllegalArgumentException("Pressing recipes require exactly one item or fluid result");
        }
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public int inputCount() {
        return inputCount;
    }

    public ItemStack itemResult() {
        return itemResult.copy();
    }

    public FluidStack fluidResult() {
        return fluidResult.copy();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return input.item().getCount() >= inputCount && ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return itemResult();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return itemResult();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(ingredient);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimalPowerFeature.PRESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AnimalPowerFeature.PRESSING_TYPE.get();
    }
}
