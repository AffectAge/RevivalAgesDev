package com.protyvkultury.revivalages.feature.technology.barrel.recipe;

import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import java.util.Arrays;
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

    private final List<CountedBarrelIngredient> ingredients;
    private final FluidStack inputFluid;
    private final FluidStack resultFluid;
    private final ItemStack resultItem;
    private final boolean requiresSeal;
    private final int processingTime;

    public BarrelRecipe(List<CountedBarrelIngredient> ingredients, FluidStack inputFluid,
            FluidStack resultFluid, ItemStack resultItem, boolean requiresSeal, int processingTime) {
        if (ingredients.isEmpty() || ingredients.size() > 4) {
            throw new IllegalArgumentException("Barrel recipes require one to four item ingredients");
        }
        if (resultFluid.isEmpty() == resultItem.isEmpty()) {
            throw new IllegalArgumentException("Barrel recipes require exactly one item or fluid result");
        }
        this.ingredients = List.copyOf(ingredients);
        this.inputFluid = Objects.requireNonNull(inputFluid).copy();
        this.resultFluid = Objects.requireNonNull(resultFluid).copy();
        this.resultItem = Objects.requireNonNull(resultItem).copy();
        this.requiresSeal = requiresSeal;
        this.processingTime = Math.max(1, processingTime);
    }

    public List<CountedBarrelIngredient> countedIngredients() {
        return ingredients;
    }

    public List<Ingredient> itemIngredients() {
        return ingredients.stream().map(CountedBarrelIngredient::ingredient).toList();
    }

    public FluidStack inputFluid() {
        return inputFluid.copy();
    }

    public FluidStack resultFluid() {
        return resultFluid.copy();
    }

    public ItemStack resultItem() {
        return resultItem.copy();
    }

    public boolean requiresSeal() {
        return requiresSeal;
    }

    public int processingTime() {
        return processingTime;
    }

    public boolean acceptsItem(ItemStack stack) {
        return ingredients.stream().anyMatch(entry -> entry.ingredient().test(stack));
    }

    @Override
    public boolean matches(BarrelRecipeInput input, Level level) {
        if (inputFluid.isEmpty()) {
            if (!input.fluid().isEmpty()) {
                return false;
            }
        } else if (!FluidStack.isSameFluidSameComponents(input.fluid(), inputFluid)
                || input.fluid().getAmount() < inputFluid.getAmount()) {
            return false;
        }
        return matchingSlots(input) != null;
    }

    /** Returns the matching physical slot for every ingredient, including overlapping tags. */
    public int[] matchingSlots(BarrelRecipeInput input) {
        int occupied = 0;
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                occupied++;
            }
        }
        if (occupied != ingredients.size()) {
            return null;
        }
        int[] slots = new int[ingredients.size()];
        Arrays.fill(slots, -1);
        return matchNext(input.items(), slots, 0, 0) ? slots : null;
    }

    private boolean matchNext(List<ItemStack> items, int[] slots, int ingredientIndex, int usedSlots) {
        if (ingredientIndex == ingredients.size()) {
            return true;
        }
        CountedBarrelIngredient required = ingredients.get(ingredientIndex);
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if ((usedSlots & (1 << slot)) == 0
                    && required.ingredient().test(stack)
                    && stack.getCount() >= required.count()) {
                slots[ingredientIndex] = slot;
                if (matchNext(items, slots, ingredientIndex + 1, usedSlots | (1 << slot))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(BarrelRecipeInput input, HolderLookup.Provider registries) {
        return resultItem();
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
        return resultItem();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.copyOf(itemIngredients());
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
