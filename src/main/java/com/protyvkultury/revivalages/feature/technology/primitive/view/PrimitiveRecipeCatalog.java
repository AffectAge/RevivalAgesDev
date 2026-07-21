package com.protyvkultury.revivalages.feature.technology.primitive.view;

import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.barrel.recipe.BarrelRecipe;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.recipe.CampfireRecipe;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.recipe.PitKilnRecipe;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.recipe.SoakingPotRecipe;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.recipe.TanningRackRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
                            Component.translatable("gui.revivalages.recipe.inherited_smelting"),
                            holder));
        }
        return result;
    }

    public static List<PrimitiveRecipeView> chopping(RecipeManager manager) {
        return manager.getAllRecipesFor(ChoppingBlockFeature.RECIPE_TYPE.get()).stream()
                .map(
                        holder -> {
                            ChoppingRecipe recipe = holder.value();
                            String chops = recipe.chops().isEmpty() ? "configured" : recipe.chops().toString();
                            String quantities =
                                    recipe.quantities().isEmpty() ? "configured" : recipe.quantities().toString();
                            return new PrimitiveRecipeView(
                                    holder.id(),
                                    List.of(recipe.ingredient()),
                                    FluidStack.EMPTY,
                                    List.of(recipe.result()),
                                    FluidStack.EMPTY,
                                    0,
                                    Component.translatable(
                                            "gui.revivalages.recipe.chopping_detail", chops, quantities),
                                    holder);
                        })
                .toList();
    }

    public static List<PrimitiveRecipeView> pitKiln(RecipeManager manager) {
        return manager.getAllRecipesFor(PitKilnFeature.RECIPE_TYPE.get()).stream()
                .map(
                        holder -> {
                            PitKilnRecipe recipe = holder.value();
                            List<ItemStack> outputs = new ArrayList<>();
                            outputs.add(recipe.result());
                            outputs.addAll(recipe.failureResults());
                            return new PrimitiveRecipeView(
                                    holder.id(),
                                    List.of(recipe.ingredient()),
                                    FluidStack.EMPTY,
                                    outputs,
                                    FluidStack.EMPTY,
                                    recipe.burnTime(),
                                    Component.translatable(
                                            "gui.revivalages.recipe.failure_chance",
                                            String.format(Locale.ROOT, "%.0f%%", recipe.failureChance() * 100.0F)),
                                    holder);
                        })
                .toList();
    }

    public static List<PrimitiveRecipeView> barrel(RecipeManager manager) {
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
                                    Component.translatable("gui.revivalages.recipe.requires_lid"),
                                    holder);
                        })
                .toList();
    }

    public static List<PrimitiveRecipeView> soakingPot(RecipeManager manager) {
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
                                    recipe.requiresCampfire()
                                            ? Component.translatable("gui.revivalages.recipe.requires_campfire")
                                            : Component.empty(),
                                    holder);
                        })
                .toList();
    }

    public static List<PrimitiveRecipeView> tanningRack(RecipeManager manager) {
        return manager.getAllRecipesFor(TanningRackFeature.RECIPE_TYPE.get()).stream()
                .map(
                        holder -> {
                            TanningRackRecipe recipe = holder.value();
                            List<ItemStack> outputs =
                                    recipe.rainFailure().isEmpty()
                                            ? List.of(recipe.result())
                                            : List.of(recipe.result(), recipe.rainFailure());
                            return new PrimitiveRecipeView(
                                    holder.id(),
                                    List.of(recipe.ingredient()),
                                    FluidStack.EMPTY,
                                    outputs,
                                    FluidStack.EMPTY,
                                    recipe.processingTime(),
                                    recipe.rainFailure().isEmpty()
                                            ? Component.translatable("gui.revivalages.recipe.requires_sun")
                                            : Component.translatable("gui.revivalages.recipe.rain_failure"),
                                    holder);
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
