package com.protyvkultury.revivalages.feature.technology.primitive.view;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.core.process.ProcessRule;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.core.process.ProcessOutcomeMode;
import com.protyvkultury.revivalages.core.process.ToolRequirementView;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.barrel.recipe.BarrelRecipe;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.recipe.CampfireRecipe;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingToolPolicy;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.recipe.PitKilnRecipe;
import com.protyvkultury.revivalages.feature.technology.pitburn.PitBurnFeature;
import com.protyvkultury.revivalages.feature.technology.pitburn.recipe.PitBurnRecipe;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.recipe.SoakingPotRecipe;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.recipe.TanningRackRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

public final class PrimitiveRecipeCatalog {

    private PrimitiveRecipeCatalog() {}

    public static List<PrimitiveRecipeView> campfire(
            RecipeManager manager, HolderLookup.Provider registries) {
        if (!ContentAvailability.isEnabled(ContentKey.CAMPFIRE)) {
            return List.of();
        }
        List<PrimitiveRecipeView> result = new ArrayList<>();
        List<RecipeHolder<CampfireRecipe>> custom =
                manager.getAllRecipesFor(CampfireFeature.RECIPE_TYPE.get());
        for (RecipeHolder<CampfireRecipe> holder : custom) {
            CampfireRecipe recipe = holder.value();
            result.add(view(holder, List.of(recipe.ingredient()), recipe.result(), recipe.cookingTime()));
        }
        for (var holder : manager.getAllRecipesFor(RecipeType.SMELTING)) {
            AbstractCookingRecipe recipe = holder.value();
            Ingredient ingredient = recipe.getIngredients().getFirst();
            if (ingredient.test(new ItemStack(Items.BREAD))
                    || ingredient.test(new ItemStack(Items.COOKIE))
                    || shadowedByCustom(ingredient, custom)) {
                continue;
            }
            result.add(
                    new PrimitiveRecipeView(
                            holder.id(),
                            List.of(ingredient),
                            FluidStack.EMPTY,
                            List.of(recipe.getResultItem(registries)),
                            FluidStack.EMPTY,
                            1800,
                            Component.empty(),
                            holder));
        }
        return result;
    }

    public static List<PrimitiveRecipeView> choppingBlock(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.CHOPPING_BLOCK)) {
            return List.of();
        }
        return manager.getAllRecipesFor(ChoppingBlockFeature.RECIPE_TYPE.get()).stream()
                .map(
                        holder -> {
                            ChoppingRecipe recipe = holder.value();
                            return new PrimitiveRecipeView(
                                    holder.id(),
                                    List.of(recipe.ingredient()),
                                    FluidStack.EMPTY,
                                    List.of(recipe.result()),
                                    FluidStack.EMPTY,
                                    0,
                                    Component.empty(),
                                    holder,
                                    List.of(),
                                    List.of(choppingToolRequirement(recipe)));
                        })
                .toList();
    }

    public static List<PrimitiveRecipeView> pitKiln(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.PIT_KILN)) {
            return List.of();
        }
        return manager.getAllRecipesFor(PitKilnFeature.RECIPE_TYPE.get()).stream()
                .map(
                        holder -> {
                            PitKilnRecipe recipe = holder.value();
                            return new PrimitiveRecipeView(
                                    holder.id(),
                                    List.of(recipe.ingredient()),
                                    FluidStack.EMPTY,
                                    failureOutputs(recipe.result(), recipe.failureChance(), recipe.failureResults()),
                                    FluidStack.EMPTY,
                                    recipe.burnTime(),
                                    Component.empty(),
                                    holder,
                                    recipe.failureChance() <= 0.0F ? List.of() : List.of(ProcessRuleView.chance(
                                            recipe.failureChance(), ProcessOutcomeMode.PER_INPUT, 0,
                                            recipe.failureResults())));
                        })
                .toList();
    }

    public static List<PrimitiveRecipeView> pitBurn(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.PIT_BURN)) {
            return List.of();
        }
        return manager.getAllRecipesFor(PitBurnFeature.RECIPE_TYPE.get()).stream()
                .map(holder -> {
                    PitBurnRecipe recipe = holder.value();
                    return new PrimitiveRecipeView(
                            holder.id(),
                            List.of(recipe.ingredient()),
                            FluidStack.EMPTY,
                            failureOutputs(recipe.result(), recipe.failureChance(), recipe.failureResults()),
                            FluidStack.EMPTY,
                            recipe.burnTime(),
                            Component.empty(),
                            holder,
                            recipe.failureChance() <= 0.0F ? List.of() : List.of(ProcessRuleView.chance(
                                    recipe.failureChance(), ProcessOutcomeMode.PER_STAGE, recipe.stages(),
                                    recipe.failureResults())));
                })
                .toList();
    }

    public static List<PrimitiveRecipeView> barrel(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.BARREL)) {
            return List.of();
        }
        return manager.getAllRecipesFor(BarrelFeature.RECIPE_TYPE.get()).stream()
                .map(
                        holder -> {
                            BarrelRecipe recipe = holder.value();
                            return new PrimitiveRecipeView(
                                    holder.id(),
                                    recipe.itemIngredients(),
                                    recipe.inputFluid(),
                                    List.of(),
                                    recipe.resultFluid(),
                                    recipe.processingTime(),
                                    Component.empty(),
                                    holder,
                                    List.of(new ProcessRuleView(ProcessRule.of(ProcessRuleType.SEALED_MACHINE))));
                        })
                .toList();
    }

    public static List<PrimitiveRecipeView> soakingPot(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.SOAKING_POT)) {
            return List.of();
        }
        return manager.getAllRecipesFor(SoakingPotFeature.RECIPE_TYPE.get()).stream()
                .map(
                        holder -> {
                            SoakingPotRecipe recipe = holder.value();
                            return new PrimitiveRecipeView(
                                    holder.id(),
                                    List.of(recipe.ingredient()),
                                    recipe.inputFluid(),
                                    List.of(recipe.result()),
                                    FluidStack.EMPTY,
                                    recipe.processingTime(),
                                    Component.empty(),
                                    holder,
                                    recipe.processRules().stream().map(ProcessRuleView::new).toList());
                        })
                .toList();
    }

    public static List<PrimitiveRecipeView> tanningRack(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.TANNING_RACK)) {
            return List.of();
        }
        return manager.getAllRecipesFor(TanningRackFeature.RECIPE_TYPE.get()).stream()
                .map(
                        holder -> {
                            TanningRackRecipe recipe = holder.value();
                            List<ItemStack> outputs =
                                    recipe.rainFailure().isEmpty()
                                            ? List.of(recipe.result())
                                    : List.of(recipe.result(), recipe.rainFailure());
                            List<ProcessRuleView> processRules = new ArrayList<>();
                            processRules.add(new ProcessRuleView(ProcessRule.of(ProcessRuleType.OPEN_SKY)));
                            if (!recipe.rainFailure().isEmpty()) {
                                processRules.add(new ProcessRuleView(
                                        ProcessRule.of(ProcessRuleType.WEATHER_EXPOSURE), recipe.rainFailure()));
                            }
                            return new PrimitiveRecipeView(
                                    holder.id(),
                                    List.of(recipe.ingredient()),
                                    FluidStack.EMPTY,
                                    outputs,
                                    FluidStack.EMPTY,
                                    recipe.processingTime(),
                                    Component.empty(),
                                    holder,
                                    processRules);
                        })
                .toList();
    }

    private static PrimitiveRecipeView view(
            RecipeHolder<?> holder, List<Ingredient> inputs, ItemStack output, int time) {
        return new PrimitiveRecipeView(
                holder.id(),
                inputs,
                FluidStack.EMPTY,
                List.of(output),
                FluidStack.EMPTY,
                time,
                Component.empty(),
                holder);
    }

    private static ToolRequirementView choppingToolRequirement(ChoppingRecipe recipe) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("gui.revivalages.tool_requirement.chopping"));
        for (int tier = 0; tier < 4; tier++) {
            tooltip.add(Component.translatable(
                    "gui.revivalages.tool_requirement.chopping_tier",
                    Component.translatable("gui.revivalages.tool_tier." + tier),
                    ChoppingToolPolicy.requiredChops(recipe, tier),
                    ChoppingToolPolicy.outputQuantity(recipe, tier)));
        }
        return new ToolRequirementView(ChoppingToolPolicy.displayIngredient(), tooltip);
    }

    private static List<ItemStack> failureOutputs(ItemStack result, float chance, List<ItemStack> failures) {
        if (chance <= 0.0F || failures.isEmpty()) {
            return List.of(result);
        }
        List<ItemStack> outputs = new ArrayList<>(failures.size() + 1);
        outputs.add(result);
        outputs.addAll(failures);
        return List.copyOf(outputs);
    }

    private static boolean shadowedByCustom(
            Ingredient inherited, List<RecipeHolder<CampfireRecipe>> custom) {
        for (ItemStack display : inherited.getItems()) {
            if (custom.stream().anyMatch(holder -> holder.value().ingredient().test(display))) {
                return true;
            }
        }
        return false;
    }
}
