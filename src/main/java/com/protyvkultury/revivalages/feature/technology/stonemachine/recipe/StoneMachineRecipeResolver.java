package com.protyvkultury.revivalages.feature.technology.stonemachine.recipe;

import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipe;
import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipeResolver;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.recipe.PitKilnRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public final class StoneMachineRecipeResolver {

    private StoneMachineRecipeResolver() {
    }

    public static Optional<StoneMachineProcess> find(
            Level level,
            StoneMachineKind kind,
            ItemStack input,
            ItemStack blade
    ) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        return switch (kind) {
            case SAWMILL -> findSawmill(level, input, blade);
            case OVEN -> findOven(level, input);
            case KILN -> findKiln(level, input);
            case CRUCIBLE -> findCrucible(level, input);
        };
    }

    private static Optional<StoneMachineProcess> findSawmill(Level level, ItemStack input, ItemStack blade) {
        BladeTier tier = bladeTier(blade);
        if (tier == null) {
            return Optional.empty();
        }
        return level.getRecipeManager()
                .getRecipeFor(ChoppingBlockFeature.RECIPE_TYPE.get(), new SingleRecipeInput(input), level)
                .map(holder -> sawmillProcess(holder, tier));
    }

    private static StoneMachineProcess sawmillProcess(RecipeHolder<ChoppingRecipe> holder, BladeTier tier) {
        ItemStack result = holder.value().result();
        result.setCount(tier.outputCount);
        return new StoneMachineProcess(
                holder.id(),
                holder.value().ingredient(),
                result,
                FluidStack.EMPTY,
                tier.processingTime,
                0.0F,
                List.of(),
                tier.woodChips
        );
    }

    private static Optional<StoneMachineProcess> findOven(Level level, ItemStack input) {
        Optional<RecipeHolder<DryingRecipe>> drying = DryingRecipeResolver.find(level, input, true);
        if (drying.isPresent()) {
            RecipeHolder<DryingRecipe> holder = drying.get();
            int time = Math.max(1, (int) Math.round(holder.value().dryingTime()
                    * PrimitiveTechnologyConfig.STONE_OVEN_DRYING_DURATION_MULTIPLIER.get()));
            return Optional.of(new StoneMachineProcess(
                    holder.id(),
                    holder.value().ingredient(),
                    holder.value().getResultItem(level.registryAccess()),
                    FluidStack.EMPTY,
                    time,
                    0.0F,
                    List.of(),
                    0
            ));
        }
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level)
                .filter(holder -> holder.value().getResultItem(level.registryAccess()).has(DataComponents.FOOD))
                .map(holder -> ovenFoodProcess(holder, level));
    }

    private static StoneMachineProcess ovenFoodProcess(
            RecipeHolder<net.minecraft.world.item.crafting.SmeltingRecipe> holder,
            Level level
    ) {
        AbstractCookingRecipe recipe = holder.value();
        return new StoneMachineProcess(
                holder.id(),
                recipe.getIngredients().getFirst(),
                recipe.getResultItem(level.registryAccess()),
                FluidStack.EMPTY,
                PrimitiveTechnologyConfig.STONE_OVEN_COOK_TICKS.get(),
                0.0F,
                List.of(),
                0
        );
    }

    private static Optional<StoneMachineProcess> findKiln(Level level, ItemStack input) {
        Optional<RecipeHolder<StoneKilnRecipe>> own = level.getRecipeManager().getRecipeFor(
                StoneMachineFeature.STONE_KILN_RECIPE_TYPE.get(),
                new SingleRecipeInput(input),
                level
        );
        if (own.isPresent()) {
            StoneKilnRecipe recipe = own.get().value();
            return Optional.of(new StoneMachineProcess(
                    own.get().id(),
                    recipe.ingredient(),
                    recipe.result(),
                    FluidStack.EMPTY,
                    recipe.processingTime(),
                    recipe.failureChance(),
                    recipe.failureResults(),
                    0
            ));
        }
        return level.getRecipeManager()
                .getRecipeFor(PitKilnFeature.RECIPE_TYPE.get(), new SingleRecipeInput(input), level)
                .map(StoneMachineRecipeResolver::inheritedKilnProcess);
    }

    private static StoneMachineProcess inheritedKilnProcess(RecipeHolder<PitKilnRecipe> holder) {
        PitKilnRecipe recipe = holder.value();
        int time = Math.max(1, (int) Math.round(recipe.burnTime()
                * PrimitiveTechnologyConfig.STONE_KILN_PIT_DURATION_MULTIPLIER.get()));
        float failure = (float) (recipe.failureChance()
                * PrimitiveTechnologyConfig.STONE_KILN_PIT_FAILURE_MULTIPLIER.get());
        return new StoneMachineProcess(
                holder.id(),
                recipe.ingredient(),
                recipe.result(),
                FluidStack.EMPTY,
                time,
                failure,
                recipe.failureResults(),
                0
        );
    }

    private static Optional<StoneMachineProcess> findCrucible(Level level, ItemStack input) {
        return level.getRecipeManager().getRecipeFor(
                        StoneMachineFeature.STONE_CRUCIBLE_RECIPE_TYPE.get(),
                        new SingleRecipeInput(input),
                        level
                )
                .map(holder -> {
                    StoneCrucibleRecipe recipe = holder.value();
                    return new StoneMachineProcess(
                            holder.id(),
                            recipe.ingredient(),
                            ItemStack.EMPTY,
                            recipe.result(),
                            recipe.processingTime(),
                            0.0F,
                            List.of(),
                            0
                    );
                });
    }

    public static List<StoneMachineProcess> all(Level level, StoneMachineKind kind) {
        List<StoneMachineProcess> result = new ArrayList<>();
        switch (kind) {
            case SAWMILL -> level.getRecipeManager().getAllRecipesFor(ChoppingBlockFeature.RECIPE_TYPE.get())
                    .forEach(holder -> result.add(sawmillProcess(holder, BladeTier.STONE)));
            case OVEN -> {
                addDrying(level, result, DryingRackFeature.CRUDE_DRYING_RECIPE_TYPE.get());
                addDrying(level, result, DryingRackFeature.DRYING_RECIPE_TYPE.get());
                level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING).stream()
                        .filter(holder -> holder.value().getResultItem(level.registryAccess()).has(DataComponents.FOOD))
                        .forEach(holder -> result.add(ovenFoodProcess(holder, level)));
            }
            case KILN -> {
                level.getRecipeManager().getAllRecipesFor(StoneMachineFeature.STONE_KILN_RECIPE_TYPE.get())
                        .forEach(holder -> {
                            StoneKilnRecipe recipe = holder.value();
                            result.add(new StoneMachineProcess(
                                    holder.id(), recipe.ingredient(), recipe.result(), FluidStack.EMPTY,
                                    recipe.processingTime(), recipe.failureChance(), recipe.failureResults(), 0));
                        });
                level.getRecipeManager().getAllRecipesFor(PitKilnFeature.RECIPE_TYPE.get())
                        .forEach(holder -> result.add(inheritedKilnProcess(holder)));
            }
            case CRUCIBLE -> level.getRecipeManager()
                    .getAllRecipesFor(StoneMachineFeature.STONE_CRUCIBLE_RECIPE_TYPE.get())
                    .forEach(holder -> {
                        StoneCrucibleRecipe recipe = holder.value();
                        result.add(new StoneMachineProcess(
                                holder.id(), recipe.ingredient(), ItemStack.EMPTY, recipe.result(),
                                recipe.processingTime(), 0.0F, List.of(), 0));
                    });
        }
        result.sort(Comparator.comparing(process -> process.sourceId().toString()));
        return List.copyOf(result);
    }

    private static void addDrying(
            Level level,
            List<StoneMachineProcess> output,
            RecipeType<DryingRecipe> recipeType
    ) {
        level.getRecipeManager().getAllRecipesFor(recipeType).forEach(holder -> {
            DryingRecipe recipe = holder.value();
            int time = Math.max(1, (int) Math.round(recipe.dryingTime()
                    * PrimitiveTechnologyConfig.STONE_OVEN_DRYING_DURATION_MULTIPLIER.get()));
            output.add(new StoneMachineProcess(
                    holder.id(), recipe.ingredient(), recipe.getResultItem(level.registryAccess()), FluidStack.EMPTY,
                    time, 0.0F, List.of(), 0));
        });
    }

    private static BladeTier bladeTier(ItemStack stack) {
        if (stack.is(StoneMachineFeature.STONE_SAW_BLADE.get())) {
            return BladeTier.STONE;
        }
        if (stack.is(StoneMachineFeature.FLINT_SAW_BLADE.get())
                || stack.is(StoneMachineFeature.BONE_SAW_BLADE.get())) {
            return BladeTier.FLINT_OR_BONE;
        }
        return null;
    }

    private enum BladeTier {
        STONE(1, 12 * 20, 4),
        FLINT_OR_BONE(2, 8 * 20, 2);

        private final int outputCount;
        private final int processingTime;
        private final int woodChips;

        BladeTier(int outputCount, int processingTime, int woodChips) {
            this.outputCount = outputCount;
            this.processingTime = processingTime;
            this.woodChips = woodChips;
        }
    }
}
