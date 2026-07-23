package com.protyvkultury.revivalages.feature.technology.constructionframe.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Immutable recipe input exposing all 27 physical frame cells. */
public final class FrameAssemblyInput implements RecipeInput {

    private final List<ItemStack> items;

    public FrameAssemblyInput(List<ItemStack> items) {
        if (items.size() != 27) {
            throw new IllegalArgumentException("Frame assembly input requires exactly 27 cells");
        }
        this.items = items.stream().map(ItemStack::copy).toList();
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return 27;
    }
}
