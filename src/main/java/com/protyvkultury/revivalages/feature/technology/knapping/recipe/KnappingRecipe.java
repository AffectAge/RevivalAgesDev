package com.protyvkultury.revivalages.feature.technology.knapping.recipe;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingFeature;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class KnappingRecipe implements Recipe<SingleRecipeInput> {

    private final ResourceLocation knappingType;
    private final KnappingPattern pattern;
    private final Optional<Ingredient> ingredient;
    private final ItemStack result;

    public KnappingRecipe(
            ResourceLocation knappingType,
            KnappingPattern pattern,
            Optional<Ingredient> ingredient,
            ItemStack result
    ) {
        this.knappingType = knappingType;
        this.pattern = pattern;
        this.ingredient = ingredient;
        this.result = result.copy();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Knapping result cannot be empty");
        }
    }

    public ResourceLocation knappingType() {
        return knappingType;
    }

    public KnappingPattern pattern() {
        return pattern;
    }

    public Optional<Ingredient> ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result.copy();
    }

    public boolean matches(ResourceLocation type, ItemStack source, int cells) {
        return ContentAvailability.isResultEnabled(result)
                && knappingType.equals(type)
                && ingredient.map(value -> value.test(source)).orElse(true)
                && pattern.matches(cells);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ContentAvailability.isResultEnabled(result)
                && ingredient.map(value -> value.test(input.item())).orElse(true);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return KnappingFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return KnappingFeature.RECIPE_TYPE.get();
    }
}
