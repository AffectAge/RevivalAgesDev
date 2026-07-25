package com.protyvkultury.revivalages.feature.technology.knapping.view;

import com.protyvkultury.revivalages.feature.technology.knapping.recipe.KnappingPattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record KnappingRecipeView(
        ResourceLocation id,
        ResourceLocation type,
        SizedIngredient input,
        Ingredient refinement,
        KnappingPattern pattern,
        ItemStack result,
        boolean hasOffTexture,
        ItemStack viewerIcon
) {

    public KnappingRecipeView {
        result = result.copy();
        viewerIcon = viewerIcon.copy();
    }

    @Override
    public ItemStack result() {
        return result.copy();
    }

    @Override
    public ItemStack viewerIcon() {
        return viewerIcon.copy();
    }

    public SizedIngredient effectiveInput() {
        return refinement.isEmpty()
                ? input
                : new SizedIngredient(refinement, input.count());
    }
}
