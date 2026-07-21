package com.protyvkultury.revivalages.feature.technology.soakingpot.recipe;

import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public final class SoakingPotRecipe implements Recipe<SoakingRecipeInput> {

    private final Ingredient ingredient;
    private final FluidStack inputFluid;
    private final ItemStack result;
    private final boolean requiresCampfire;
    private final int processingTime;

    public SoakingPotRecipe(
            Ingredient ingredient,
            FluidStack inputFluid,
            ItemStack result,
            boolean requiresCampfire,
            int processingTime
    ) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.inputFluid = Objects.requireNonNull(inputFluid).copy();
        this.result = Objects.requireNonNull(result).copy();
        this.requiresCampfire = requiresCampfire;
        this.processingTime = Math.max(1, processingTime);
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public FluidStack inputFluid() {
        return inputFluid.copy();
    }

    public ItemStack result() {
        return result.copy();
    }

    public boolean requiresCampfire() {
        return requiresCampfire;
    }

    public int processingTime() {
        return processingTime;
    }

    @Override
    public boolean matches(SoakingRecipeInput input, Level level) {
        return ingredient.test(input.item())
                && FluidStack.isSameFluidSameComponents(input.fluid(), inputFluid)
                && input.fluid().getAmount() >= inputFluid.getAmount();
    }

    @Override
    public ItemStack assemble(SoakingRecipeInput input, HolderLookup.Provider registries) {
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
        return SoakingPotFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SoakingPotFeature.RECIPE_TYPE.get();
    }
}
