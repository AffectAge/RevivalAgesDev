package com.protyvkultury.revivalages.feature.technology.soakingpot.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record SoakingRecipeInput(ItemStack item, FluidStack fluid) implements RecipeInput {

    public SoakingRecipeInput {
        item = item.copy();
        fluid = fluid.copy();
    }

    @Override
    public ItemStack getItem(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException(index);
        }
        return item;
    }

    @Override
    public int size() {
        return 1;
    }
}
