package com.protyvkultury.revivalages.feature.technology.animalpower.recipe;

import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
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

public final class GrindingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final int inputCount;
    private final ItemStack result;
    private final ItemStack secondaryResult;
    private final double secondaryChance;
    private final int workPoints;
    private final List<GrindingMachine> machines;

    public GrindingRecipe(
            Ingredient ingredient,
            int inputCount,
            ItemStack result,
            ItemStack secondaryResult,
            double secondaryChance,
            int workPoints,
            List<GrindingMachine> machines
    ) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.inputCount = inputCount;
        this.result = Objects.requireNonNull(result).copy();
        this.secondaryResult = Objects.requireNonNull(secondaryResult).copy();
        this.secondaryChance = secondaryChance;
        this.workPoints = workPoints;
        this.machines = List.copyOf(machines);
        if (inputCount <= 0) {
            throw new IllegalArgumentException("input_count must be positive");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("result cannot be empty");
        }
        if (secondaryChance < 0.0D || secondaryChance > 1.0D) {
            throw new IllegalArgumentException("secondary_chance must be between zero and one");
        }
        if (secondaryResult.isEmpty() && secondaryChance != 0.0D) {
            throw new IllegalArgumentException("secondary_chance requires secondary_result");
        }
        if (workPoints <= 0) {
            throw new IllegalArgumentException("work_points must be positive");
        }
        if (machines.isEmpty() || machines.stream().distinct().count() != machines.size()) {
            throw new IllegalArgumentException("machines must contain unique values and cannot be empty");
        }
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public int inputCount() {
        return inputCount;
    }

    public ItemStack result() {
        return result.copy();
    }

    public ItemStack secondaryResult() {
        return secondaryResult.copy();
    }

    public double secondaryChance() {
        return secondaryChance;
    }

    public int workPoints() {
        return workPoints;
    }

    public List<GrindingMachine> machines() {
        return machines;
    }

    public boolean supports(GrindingMachine machine) {
        return machines.contains(machine);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return input.item().getCount() >= inputCount && ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(ingredient);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimalPowerFeature.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AnimalPowerFeature.GRINDING_TYPE.get();
    }
}
