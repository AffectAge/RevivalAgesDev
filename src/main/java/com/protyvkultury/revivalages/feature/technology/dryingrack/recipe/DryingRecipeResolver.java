package com.protyvkultury.revivalages.feature.technology.dryingrack.recipe;

import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class DryingRecipeResolver {

    private DryingRecipeResolver() {
    }

    public static Optional<RecipeHolder<DryingRecipe>> find(Level level, ItemStack stack, boolean normalRack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        if (normalRack) {
            Optional<RecipeHolder<DryingRecipe>> own = find(level, stack, DryingRackFeature.DRYING_RECIPE_TYPE.get());
            if (own.isPresent()) {
                return own;
            }
        }
        return find(level, stack, DryingRackFeature.CRUDE_DRYING_RECIPE_TYPE.get());
    }

    private static Optional<RecipeHolder<DryingRecipe>> find(
            Level level,
            ItemStack stack,
            RecipeType<DryingRecipe> type
    ) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return level.getRecipeManager().getRecipesFor(type, input, level).stream()
                .sorted(Comparator.comparing(holder -> holder.id().toString()))
                .findFirst();
    }
}
