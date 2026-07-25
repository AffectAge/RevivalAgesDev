package com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe;

import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityFeature;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTransformationRecipe implements Recipe<RecipeInput> {

    public enum Kind {
        COLLAPSE,
        LANDSLIDE
    }

    private final Kind kind;
    private final BlockIngredient ingredient;
    private final BlockState result;

    public BlockTransformationRecipe(Kind kind, BlockIngredient ingredient, BlockState result) {
        this.kind = Objects.requireNonNull(kind);
        this.ingredient = Objects.requireNonNull(ingredient);
        this.result = Objects.requireNonNull(result);
    }

    public Kind kind() {
        return kind;
    }

    public BlockIngredient ingredient() {
        return ingredient;
    }

    public BlockState result() {
        return result;
    }

    public boolean matches(BlockState state) {
        return ingredient.test(state);
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return kind == Kind.COLLAPSE
                ? StructuralIntegrityFeature.COLLAPSE_SERIALIZER.get()
                : StructuralIntegrityFeature.LANDSLIDE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return kind == Kind.COLLAPSE
                ? StructuralIntegrityFeature.COLLAPSE_TYPE.get()
                : StructuralIntegrityFeature.LANDSLIDE_TYPE.get();
    }
}
