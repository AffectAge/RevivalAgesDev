package com.protyvkultury.revivalages.api.weight;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/**
 * Resolves a per-unit weight for supported item stacks.
 */
@FunctionalInterface
public interface ItemWeightProvider {

    Optional<WeightResult> getWeight(ItemStack stack, WeightContext context, WeightLookup lookup);
}
