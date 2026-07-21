package com.protyvkultury.revivalages.feature.technology.barrel.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record BarrelRecipeInput(List<ItemStack> items, FluidStack fluid) implements RecipeInput {

    public BarrelRecipeInput {
        items = items.stream().map(ItemStack::copy).toList();
        fluid = fluid.copy();
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
