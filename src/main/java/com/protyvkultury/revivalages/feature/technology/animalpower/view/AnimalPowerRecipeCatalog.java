package com.protyvkultury.revivalages.feature.technology.animalpower.view;

import com.protyvkultury.revivalages.core.process.ProcessOutcomeMode;
import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

/** Loader-neutral views derived directly from the canonical gameplay recipes. */
public final class AnimalPowerRecipeCatalog {

    private AnimalPowerRecipeCatalog() {
    }

    public static List<PrimitiveRecipeView> handGrinding(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.HAND_GRINDSTONE)) {
            return List.of();
        }
        return manager.getAllRecipesFor(AnimalPowerFeature.GRINDING_TYPE.get()).stream()
                .map(AnimalPowerRecipeCatalog::handGrinding)
                .toList();
    }

    private static PrimitiveRecipeView handGrinding(RecipeHolder<GrindingRecipe> holder) {
        return grindingView(holder);
    }

    private static PrimitiveRecipeView grindingView(RecipeHolder<GrindingRecipe> holder) {
        GrindingRecipe recipe = holder.value();
        List<ProcessRuleView> rules = new java.util.ArrayList<>();
        if (!recipe.secondaryResult().isEmpty()) {
            rules.add(ProcessRuleView.chance(
                    recipe.secondaryChance(), ProcessOutcomeMode.ADDITIONAL, 0, List.of(recipe.secondaryResult())));
        }
        return new PrimitiveRecipeView(
                holder.id(),
                List.of(recipe.ingredient()),
                FluidStack.EMPTY,
                List.of(recipe.result()),
                FluidStack.EMPTY,
                0,
                Component.empty(),
                holder,
                rules
        );
    }

}
