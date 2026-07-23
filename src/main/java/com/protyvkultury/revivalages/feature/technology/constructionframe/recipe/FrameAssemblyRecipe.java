package com.protyvkultury.revivalages.feature.technology.constructionframe.recipe;

import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameFeature;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Canonical 3x3x3 frame recipe. Matching permits Y rotation but never mirroring. */
public final class FrameAssemblyRecipe implements Recipe<FrameAssemblyInput> {

    private final NonNullList<Ingredient> ingredients;
    private final Ingredient tool;
    private final ItemStack result;
    private final Optional<FrameGridPosition> woodVariantSource;

    public FrameAssemblyRecipe(
            List<Ingredient> ingredients,
            Ingredient tool,
            ItemStack result,
            Optional<FrameGridPosition> woodVariantSource
    ) {
        if (ingredients.size() != 27) {
            throw new IllegalArgumentException("Frame assembly recipe requires exactly 27 ingredients");
        }
        if (tool.isEmpty()) {
            throw new IllegalArgumentException("Frame assembly tool cannot be empty");
        }
        if (result.getCount() != 1 || !(result.getItem() instanceof BlockItem)) {
            throw new IllegalArgumentException("Frame assembly result must be one BlockItem");
        }
        this.ingredients = NonNullList.create();
        this.ingredients.addAll(ingredients);
        this.tool = tool;
        this.result = result.copy();
        this.woodVariantSource = woodVariantSource;
    }

    public Ingredient tool() {
        return tool;
    }

    public ItemStack result() {
        return result.copy();
    }

    public Optional<FrameGridPosition> woodVariantSource() {
        return woodVariantSource;
    }

    public boolean matchesTool(ItemStack stack) {
        return !stack.isEmpty() && tool.test(stack);
    }

    @Override
    public boolean matches(FrameAssemblyInput input, Level level) {
        for (int rotations = 0; rotations < 4; rotations++) {
            if (matchesRotation(input, rotations)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesRotation(FrameAssemblyInput input, int rotations) {
        for (int index = 0; index < 27; index++) {
            FrameGridPosition expected = FrameGridPosition.fromIndex(index);
            int actualIndex = expected.rotatedIndex(rotations);
            if (!ingredients.get(index).test(input.getItem(actualIndex))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(FrameAssemblyInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ConstructionFrameFeature.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ConstructionFrameFeature.RECIPE_TYPE.get();
    }
}
