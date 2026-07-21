package com.protyvkultury.revivalages.feature.technology.campfire.recipe;

import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class CampfireRecipeResolver {

    private CampfireRecipeResolver() {
    }

    public static Optional<Match> find(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        Optional<RecipeHolder<CampfireRecipe>> custom = level.getRecipeManager()
                .getRecipeFor(CampfireFeature.RECIPE_TYPE.get(), recipeInput, level);
        if (custom.isPresent()) {
            RecipeHolder<CampfireRecipe> holder = custom.get();
            return Optional.of(new Match(
                    holder.id(),
                    holder.value().result(),
                    holder.value().cookingTime(),
                    false
            ));
        }
        if (input.is(Items.BREAD) || input.is(Items.COOKIE)) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, recipeInput, level)
                .map(holder -> fromCooking(holder, level));
    }

    private static Match fromCooking(
            RecipeHolder<? extends AbstractCookingRecipe> holder,
            Level level
    ) {
        return new Match(
                holder.id(),
                holder.value().getResultItem(level.registryAccess()).copy(),
                PrimitiveTechnologyConfig.CAMPFIRE_COOK_TICKS.get(),
                true
        );
    }

    public record Match(ResourceLocation id, ItemStack output, int cookingTime, boolean inherited) {

        public Match {
            output = output.copy();
            cookingTime = Math.max(1, cookingTime);
        }
    }
}
