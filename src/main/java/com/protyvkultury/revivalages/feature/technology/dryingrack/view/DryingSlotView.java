package com.protyvkultury.revivalages.feature.technology.dryingrack.view;

import net.minecraft.world.item.ItemStack;

/**
 * Immutable, loader-neutral slot state for probe and recipe-viewer integrations.
 */
public record DryingSlotView(
        ItemStack stack,
        ItemStack recipeOutput,
        double progress,
        boolean processing,
        boolean completed
) {

    public DryingSlotView {
        stack = stack.copy();
        recipeOutput = recipeOutput.copy();
        progress = Math.clamp(progress, 0.0D, 1.0D);
    }
}
