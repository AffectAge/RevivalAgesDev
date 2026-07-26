package com.protyvkultury.revivalages.api.weight;

import net.minecraft.world.item.ItemStack;

/**
 * Recursive lookup boundary supplied to item-weight providers.
 */
@FunctionalInterface
public interface WeightLookup {

    WeightResult getWeight(ItemStack stack, WeightContext context);
}
