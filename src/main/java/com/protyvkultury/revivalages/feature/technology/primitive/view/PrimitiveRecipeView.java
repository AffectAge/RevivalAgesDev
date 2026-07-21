package com.protyvkultury.revivalages.feature.technology.primitive.view;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

public record PrimitiveRecipeView(
        ResourceLocation id,
        List<Ingredient> itemInputs,
        FluidStack fluidInput,
        List<ItemStack> itemOutputs,
        FluidStack fluidOutput,
        int processingTime,
        Component detail,
        RecipeHolder<?> backingRecipe) {
    public PrimitiveRecipeView {
        itemInputs = List.copyOf(itemInputs);
        fluidInput = fluidInput.copy();
        itemOutputs = itemOutputs.stream().map(ItemStack::copy).toList();
        fluidOutput = fluidOutput.copy();
    }
}
