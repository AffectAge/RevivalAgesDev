package com.protyvkultury.revivalages.feature.technology.choppingblock.recipe;

import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveProcessingMath;
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

public final class ChoppingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final List<Integer> chops;
    private final List<Integer> quantities;

    public ChoppingRecipe(Ingredient ingredient, ItemStack result, List<Integer> chops, List<Integer> quantities) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.result = Objects.requireNonNull(result).copy();
        this.chops = List.copyOf(chops);
        this.quantities = List.copyOf(quantities);
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result.copy();
    }

    public List<Integer> chops() {
        return chops;
    }

    public List<Integer> quantities() {
        return quantities;
    }

    public int chopsForTier(int tier, int fallback) {
        return PrimitiveProcessingMath.tierValue(chops, tier, fallback, 1);
    }

    public int quantityForTier(int tier, int fallback) {
        return PrimitiveProcessingMath.tierValue(quantities, tier, fallback, 0);
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
        return ChoppingBlockFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ChoppingBlockFeature.RECIPE_TYPE.get();
    }
}
