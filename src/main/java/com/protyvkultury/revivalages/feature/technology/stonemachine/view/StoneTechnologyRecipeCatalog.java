package com.protyvkultury.revivalages.feature.technology.stonemachine.view;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.anvil.AnvilFeature;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilRecipe;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilTool;
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
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/** Shared recipe-view data for JEI and EMI. Runtime recipe behavior remains the source of truth. */
public final class StoneTechnologyRecipeCatalog {

    private StoneTechnologyRecipeCatalog() {
    }

    public static List<PrimitiveRecipeView> sawmill(RecipeManager manager) {
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
                Component.translatable(
                        "gui.revivalages.recipe.sawmill_chips",
                        String.format(Locale.ROOT, "%.0f%%",
                                PrimitiveTechnologyConfig.STONE_SAWMILL_WOOD_CHIP_CHANCE.get() * 100.0D)),
                holder);
    }

    public static List<PrimitiveRecipeView> oven(Level level) {
        return processViews(level, StoneMachineKind.OVEN, "stone_oven");
    }

    public static List<PrimitiveRecipeView> kiln(Level level) {
        return processViews(level, StoneMachineKind.KILN, "stone_kiln");
    }

    public static List<PrimitiveRecipeView> crucible(Level level) {
        return processViews(level, StoneMachineKind.CRUCIBLE, "stone_crucible");
    }

    private static List<PrimitiveRecipeView> processViews(Level level, StoneMachineKind kind, String category) {
        List<PrimitiveRecipeView> views = new ArrayList<>();
        for (StoneMachineProcess process : StoneMachineRecipeResolver.all(level, kind)) {
            List<ItemStack> outputs = new ArrayList<>();
            if (!process.itemResult().isEmpty()) {
                outputs.add(process.itemResult());
            }
            outputs.addAll(process.failureResults());
            Component detail = process.failureChance() > 0.0F
                    ? Component.translatable(
                            "gui.revivalages.recipe.failure_chance",
                            String.format(Locale.ROOT, "%.0f%%", process.failureChance() * 100.0F))
                    : Component.empty();
            views.add(new PrimitiveRecipeView(
                    derivedId(category, process.sourceId()),
                    List.of(process.ingredient()),
                    FluidStack.EMPTY,
                    outputs,
                    process.fluidResult(),
                    process.processingTime(),
                    detail,
                    level.getRecipeManager().byKey(process.sourceId()).orElse(null)));
        }
        return List.copyOf(views);
    }

    public static List<PrimitiveRecipeView> anvil(RecipeManager manager) {
        return manager.getAllRecipesFor(AnvilFeature.RECIPE_TYPE.get()).stream()
                .map(holder -> {
                    AnvilRecipe recipe = holder.value();
                    String key = recipe.tool() == AnvilTool.HAMMER
                            ? "gui.revivalages.recipe.tool.hammer"
                            : "gui.revivalages.recipe.tool.pickaxe";
                    return new PrimitiveRecipeView(
                            holder.id(),
                            List.of(recipe.ingredient()),
                            FluidStack.EMPTY,
                            List.of(recipe.result()),
                            FluidStack.EMPTY,
                            0,
                            Component.translatable("gui.revivalages.recipe.anvil_detail",
                                    Component.translatable(key), recipe.hits()),
                            holder);
                })
                .sorted(Comparator.comparing(view -> view.id().toString()))
                .toList();
    }

    private static ResourceLocation derivedId(String category, ResourceLocation source) {
        return RevivalAges.id(category + "/" + source.getNamespace() + "/" + source.getPath());
    }
}
