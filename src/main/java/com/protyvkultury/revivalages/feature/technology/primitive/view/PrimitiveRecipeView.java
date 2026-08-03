package com.protyvkultury.revivalages.feature.technology.primitive.view;

import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.core.process.ToolRequirementView;
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
        RecipeHolder<?> backingRecipe,
        List<ProcessRuleView> processRules,
        List<ToolRequirementView> toolRequirements) {

    public PrimitiveRecipeView(
            ResourceLocation id,
            List<Ingredient> itemInputs,
            FluidStack fluidInput,
            List<ItemStack> itemOutputs,
            FluidStack fluidOutput,
            int processingTime,
            Component detail,
            RecipeHolder<?> backingRecipe
    ) {
        this(
                id,
                itemInputs,
                fluidInput,
                itemOutputs,
                fluidOutput,
                processingTime,
                detail,
                backingRecipe,
                List.of(),
                List.of()
        );
    }

    public PrimitiveRecipeView(
            ResourceLocation id, List<Ingredient> itemInputs, FluidStack fluidInput, List<ItemStack> itemOutputs,
            FluidStack fluidOutput, int processingTime, Component detail, RecipeHolder<?> backingRecipe,
            List<ProcessRuleView> processRules) {
        this(id, itemInputs, fluidInput, itemOutputs, fluidOutput, processingTime, detail, backingRecipe,
                processRules, List.of());
    }

    public PrimitiveRecipeView {
        itemInputs = List.copyOf(itemInputs);
        fluidInput = fluidInput.copy();
        itemOutputs = itemOutputs.stream().map(ItemStack::copy).toList();
        fluidOutput = fluidOutput.copy();
        processRules = List.copyOf(processRules);
        toolRequirements = List.copyOf(toolRequirements);
    }
}
