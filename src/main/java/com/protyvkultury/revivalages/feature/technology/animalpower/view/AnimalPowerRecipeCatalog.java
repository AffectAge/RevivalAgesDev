package com.protyvkultury.revivalages.feature.technology.animalpower.view;

import com.protyvkultury.revivalages.core.process.ProcessRule;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.core.process.ProcessOutcomeMode;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerConfig;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalChoppingProfile;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipe;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.PressingRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    public static List<PrimitiveRecipeView> animalGrinding(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.HORSE_GRINDSTONE)) {
            return List.of();
        }
        return manager.getAllRecipesFor(AnimalPowerFeature.GRINDING_TYPE.get()).stream()
                .map(AnimalPowerRecipeCatalog::animalGrinding)
                .toList();
    }

    public static List<PrimitiveRecipeView> animalChopping(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.HORSE_CHOPPING_BLOCK)) {
            return List.of();
        }
        return manager.getAllRecipesFor(ChoppingBlockFeature.RECIPE_TYPE.get()).stream()
                .map(AnimalPowerRecipeCatalog::animalChopping)
                .toList();
    }

    public static List<PrimitiveRecipeView> pressing(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.HORSE_PRESS)) {
            return List.of();
        }
        return manager.getAllRecipesFor(AnimalPowerFeature.PRESSING_TYPE.get()).stream()
                .map(AnimalPowerRecipeCatalog::pressing)
                .toList();
    }

    private static PrimitiveRecipeView handGrinding(RecipeHolder<GrindingRecipe> holder) {
        return grindingView(holder, List.of());
    }

    private static PrimitiveRecipeView animalGrinding(RecipeHolder<GrindingRecipe> holder) {
        return grindingView(holder, animalRules());
    }

    private static PrimitiveRecipeView grindingView(RecipeHolder<GrindingRecipe> holder, List<ProcessRuleView> baseRules) {
        GrindingRecipe recipe = holder.value();
        List<ProcessRuleView> rules = new java.util.ArrayList<>(baseRules);
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

    private static PrimitiveRecipeView animalChopping(RecipeHolder<ChoppingRecipe> holder) {
        ChoppingRecipe recipe = holder.value();
        int tier = AnimalPowerConfig.CHOPPING_TIER.get();
        int cycles = recipe.chopsForTier(tier, AnimalChoppingProfile.cyclesForTier(tier));
        int outputCount = recipe.quantityForTier(tier, AnimalChoppingProfile.quantityForTier(tier));
        ItemStack output = recipe.result();
        output.setCount(outputCount);
        return new PrimitiveRecipeView(
                holder.id(),
                List.of(recipe.ingredient()),
                FluidStack.EMPTY,
                List.of(output),
                FluidStack.EMPTY,
                0,
                Component.empty(),
                holder,
                animalRules()
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
                Component.empty(),
                holder,
                animalRules()
        );
    }

    private static List<ProcessRuleView> animalRules() {
        return List.of(
                new ProcessRuleView(ProcessRule.of(ProcessRuleType.VALID_WORK_AREA)),
                new ProcessRuleView(ProcessRule.of(ProcessRuleType.ATTACHED_WORKER)));
    }
}
