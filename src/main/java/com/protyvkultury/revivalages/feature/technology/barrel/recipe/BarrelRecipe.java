package com.protyvkultury.revivalages.feature.technology.barrel.recipe;

import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import java.util.ArrayList;
import java.util.List;
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

public final class BarrelRecipe implements Recipe<BarrelRecipeInput> {

    private final List<Ingredient> ingredients;
    private final FluidStack inputFluid;
    private final FluidStack resultFluid;
    private final int processingTime;

    public BarrelRecipe(List<Ingredient> ingredients, FluidStack inputFluid, FluidStack resultFluid, int processingTime) {
        if (ingredients.isEmpty() || ingredients.size() > 4) {
            throw new IllegalArgumentException("Barrel recipes require one to four item ingredients");
        }
        this.ingredients = List.copyOf(ingredients);
        this.inputFluid = Objects.requireNonNull(inputFluid).copy();
        this.resultFluid = Objects.requireNonNull(resultFluid).copy();
        this.processingTime = Math.max(1, processingTime);
    }

    public List<Ingredient> itemIngredients() {
        return ingredients;
    }

    public FluidStack inputFluid() {
        return inputFluid.copy();
    }

    public FluidStack resultFluid() {
        return resultFluid.copy();
    }

    public int processingTime() {
        return processingTime;
    }

    public boolean acceptsItem(ItemStack stack) {
        return ingredients.stream().anyMatch(ingredient -> ingredient.test(stack));
    }

    @Override
    public boolean matches(BarrelRecipeInput input, Level level) {
        if (!FluidStack.isSameFluidSameComponents(input.fluid(), inputFluid)
                || input.fluid().getAmount() < inputFluid.getAmount()) {
            return false;
        }
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                remaining.add(stack);
            }
        }
        if (remaining.size() != ingredients.size()) {
            return false;
        }
        boolean[] used = new boolean[remaining.size()];
        for (Ingredient ingredient : ingredients) {
            boolean matched = false;
            for (int index = 0; index < remaining.size(); index++) {
                if (!used[index] && ingredient.test(remaining.get(index))) {
                    used[index] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(BarrelRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
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
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.copyOf(ingredients);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BarrelFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BarrelFeature.RECIPE_TYPE.get();
    }
}
