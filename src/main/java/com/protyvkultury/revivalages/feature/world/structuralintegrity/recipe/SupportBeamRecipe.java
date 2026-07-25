package com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe;

import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityFeature;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityTags;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public final class SupportBeamRecipe extends ShapedRecipe {

    private final ItemStack result;

    public SupportBeamRecipe(
            String group,
            CraftingBookCategory category,
            ShapedRecipePattern pattern,
            ItemStack result,
            boolean showNotification
    ) {
        super(group, category, pattern, result, showNotification);
        this.result = result.copy();
    }

    public ItemStack result() {
        return result.copy();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(input);
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (!stack.is(StructuralIntegrityTags.SAW_BLADES)) {
                continue;
            }
            ItemStack saw = stack.copyWithCount(1);
            int damage = StructuralIntegrityConfig.SAW_DAMAGE.get();
            if (!saw.isDamageableItem() || damage == 0) {
                remaining.set(index, saw);
            } else if (saw.getDamageValue() + damage < saw.getMaxDamage()) {
                saw.setDamageValue(saw.getDamageValue() + damage);
                remaining.set(index, saw);
            } else {
                remaining.set(index, ItemStack.EMPTY);
            }
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StructuralIntegrityFeature.SUPPORT_BEAM_SERIALIZER.get();
    }
}
