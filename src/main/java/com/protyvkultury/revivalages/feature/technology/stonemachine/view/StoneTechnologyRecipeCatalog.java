package com.protyvkultury.revivalages.feature.technology.stonemachine.view;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.core.process.ProcessRule;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.core.process.ProcessOutcomeMode;
import com.protyvkultury.revivalages.feature.technology.anvil.AnvilToolPolicy;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.anvil.AnvilFeature;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilRecipe;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import com.protyvkultury.revivalages.feature.technology.stonemachine.recipe.StoneMachineProcess;
import com.protyvkultury.revivalages.feature.technology.stonemachine.recipe.StoneMachineRecipeResolver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/** Shared recipe-view data for JEI and EMI. Runtime recipe behavior remains the source of truth. */
public final class StoneTechnologyRecipeCatalog {

    private StoneTechnologyRecipeCatalog() {
    }

    public static List<PrimitiveRecipeView> sawmill(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.STONE_SAWMILL)) {
            return List.of();
        }
        List<PrimitiveRecipeView> result = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(ChoppingBlockFeature.RECIPE_TYPE.get())) {
            ChoppingRecipe recipe = holder.value();
            result.add(sawmillView(holder, recipe, "stone", Ingredient.of(StoneMachineFeature.STONE_SAW_BLADE),
                    1, 12 * 20, 4));
            result.add(sawmillView(holder, recipe, "flint_or_bone",
                    Ingredient.of(StoneMachineFeature.FLINT_SAW_BLADE, StoneMachineFeature.BONE_SAW_BLADE),
                    2, 8 * 20, 2));
        }
        result.sort(Comparator.comparing(view -> view.id().toString()));
        return List.copyOf(result);
    }

    private static PrimitiveRecipeView sawmillView(
            net.minecraft.world.item.crafting.RecipeHolder<ChoppingRecipe> holder,
            ChoppingRecipe recipe,
            String tier,
            Ingredient blade,
            int outputCount,
            int processingTime,
            int woodChips
    ) {
        ItemStack output = recipe.result();
        output.setCount(outputCount);
        ItemStack chips = new ItemStack(PrimitiveMaterialsFeature.WOOD_CHIPS.get(), woodChips);
        return new PrimitiveRecipeView(
                derivedId("stone_sawmill/" + tier, holder.id()),
                List.of(recipe.ingredient(), blade),
                FluidStack.EMPTY,
                List.of(output, chips),
                FluidStack.EMPTY,
                processingTime,
                Component.empty(),
                holder,
                List.of(
                        new ProcessRuleView(ProcessRule.of(ProcessRuleType.FUELLED_AND_LIT)),
                        new ProcessRuleView(ProcessRule.of(ProcessRuleType.INSTALLED_TOOL)),
                        ProcessRuleView.chance(
                                PrimitiveTechnologyConfig.STONE_SAWMILL_WOOD_CHIP_CHANCE.get(),
                                ProcessOutcomeMode.PER_ATTEMPT,
                                woodChips,
                                List.of(chips))));
    }

    public static List<PrimitiveRecipeView> oven(Level level) {
        return ContentAvailability.isEnabled(ContentKey.STONE_OVEN)
                ? processViews(level, StoneMachineKind.OVEN, "stone_oven")
                : List.of();
    }

    public static List<PrimitiveRecipeView> kiln(Level level) {
        return ContentAvailability.isEnabled(ContentKey.STONE_KILN)
                ? processViews(level, StoneMachineKind.KILN, "stone_kiln")
                : List.of();
    }

    public static List<PrimitiveRecipeView> crucible(Level level) {
        return ContentAvailability.isEnabled(ContentKey.STONE_CRUCIBLE)
                ? processViews(level, StoneMachineKind.CRUCIBLE, "stone_crucible")
                : List.of();
    }

    private static List<PrimitiveRecipeView> processViews(Level level, StoneMachineKind kind, String category) {
        List<PrimitiveRecipeView> views = new ArrayList<>();
        for (StoneMachineProcess process : StoneMachineRecipeResolver.all(level, kind)) {
            List<ItemStack> outputs = new ArrayList<>();
            if (!process.itemResult().isEmpty()) {
                outputs.add(process.itemResult());
            }
            List<ProcessRuleView> rules = new ArrayList<>();
            rules.add(new ProcessRuleView(ProcessRule.of(ProcessRuleType.FUELLED_AND_LIT)));
            if (kind == StoneMachineKind.KILN && process.failureChance() > 0.0F) {
                List<ItemStack> failureOutcomes = kilnFailureOutcomes(process);
                outputs.addAll(failureOutcomes);
                rules.add(ProcessRuleView.chance(
                        process.failureChance(), ProcessOutcomeMode.PER_ITEM, 0, failureOutcomes));
            }
            views.add(new PrimitiveRecipeView(
                    derivedId(category, process.sourceId()),
                    List.of(process.ingredient()),
                    FluidStack.EMPTY,
                    outputs,
                    process.fluidResult(),
                    process.processingTime(),
                    Component.empty(),
                    level.getRecipeManager().byKey(process.sourceId()).orElse(null),
                    rules));
        }
        return List.copyOf(views);
    }

    static List<ItemStack> kilnFailureOutcomes(StoneMachineProcess process) {
        return process.failureResults().isEmpty()
                ? List.of(new ItemStack(PrimitiveMaterialsFeature.PIT_ASH.get()))
                : process.failureResults();
    }

    public static List<PrimitiveRecipeView> anvil(RecipeManager manager) {
        if (!ContentAvailability.isEnabled(ContentKey.ANVIL)) {
            return List.of();
        }
        return manager.getAllRecipesFor(AnvilFeature.RECIPE_TYPE.get()).stream()
                .map(holder -> {
                    AnvilRecipe recipe = holder.value();
                    return new PrimitiveRecipeView(
                            holder.id(),
                            List.of(recipe.ingredient()),
                            FluidStack.EMPTY,
                            List.of(recipe.result()),
                            FluidStack.EMPTY,
                            0,
                            Component.empty(),
                            holder,
                            List.of(new ProcessRuleView(ProcessRule.of(ProcessRuleType.REQUIRED_MANUAL_TOOL))),
                            List.of(AnvilToolPolicy.viewerRequirement(recipe)));
                })
                .sorted(Comparator.comparing(view -> view.id().toString()))
                .toList();
    }

    private static ResourceLocation derivedId(String category, ResourceLocation source) {
        return RevivalAges.id(category + "/" + source.getNamespace() + "/" + source.getPath());
    }
}
