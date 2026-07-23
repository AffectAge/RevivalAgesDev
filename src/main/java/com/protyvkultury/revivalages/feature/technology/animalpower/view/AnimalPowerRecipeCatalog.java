package com.protyvkultury.revivalages.feature.technology.animalpower.view;

import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerConfig;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipe;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.PressingRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

/** Loader-neutral views derived directly from the canonical gameplay recipes. */
public final class AnimalPowerRecipeCatalog {

    private AnimalPowerRecipeCatalog() {
    }

    public static List<PrimitiveRecipeView> grinding(RecipeManager manager) {
        return manager.getAllRecipesFor(AnimalPowerFeature.GRINDING_TYPE.get()).stream()
                .map(AnimalPowerRecipeCatalog::grinding)
                .toList();
    }

    public static List<PrimitiveRecipeView> pressing(RecipeManager manager) {
        return manager.getAllRecipesFor(AnimalPowerFeature.PRESSING_TYPE.get()).stream()
                .map(AnimalPowerRecipeCatalog::pressing)
                .toList();
    }

    private static PrimitiveRecipeView grinding(RecipeHolder<GrindingRecipe> holder) {
        GrindingRecipe recipe = holder.value();
        List<ItemStack> outputs = recipe.secondaryResult().isEmpty()
                ? List.of(recipe.result())
                : List.of(recipe.result(), recipe.secondaryResult());
        return new PrimitiveRecipeView(
                holder.id(),
                List.of(recipe.ingredient()),
                FluidStack.EMPTY,
                outputs,
                FluidStack.EMPTY,
                0,
                Component.translatable("gui.revivalages.recipe.work_points", recipe.workPoints()),
                holder
        );
    }

    private static PrimitiveRecipeView pressing(RecipeHolder<PressingRecipe> holder) {
        PressingRecipe recipe = holder.value();
        return new PrimitiveRecipeView(
                holder.id(),
                List.of(recipe.ingredient()),
                FluidStack.EMPTY,
                recipe.itemResult().isEmpty() ? List.of() : List.of(recipe.itemResult()),
                recipe.fluidResult(),
                0,
                Component.translatable("gui.revivalages.recipe.work_points", AnimalPowerConfig.PRESS_POINTS.get()),
                holder
        );
    }
}
