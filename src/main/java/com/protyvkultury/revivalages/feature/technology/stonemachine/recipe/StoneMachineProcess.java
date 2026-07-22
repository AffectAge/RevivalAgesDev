package com.protyvkultury.revivalages.feature.technology.stonemachine.recipe;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

public record StoneMachineProcess(
        ResourceLocation sourceId,
        Ingredient ingredient,
        ItemStack itemResult,
        FluidStack fluidResult,
        int processingTime,
        float failureChance,
        List<ItemStack> failureResults,
        int woodChips
) {

    public StoneMachineProcess {
        itemResult = itemResult.copy();
        fluidResult = fluidResult.copy();
        failureResults = failureResults.stream().map(ItemStack::copy).toList();
        processingTime = Math.max(1, processingTime);
        failureChance = Math.clamp(failureChance, 0.0F, 1.0F);
        woodChips = Math.max(0, woodChips);
    }
}
