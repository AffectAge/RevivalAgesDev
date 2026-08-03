package com.protyvkultury.revivalages.feature.technology.choppingblock;

import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveTags;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;

/** Canonical chopping-tool eligibility, tier, and recipe work values. */
public final class ChoppingToolPolicy {

    private ChoppingToolPolicy() {
    }

    public static boolean isUsable(ItemStack stack) {
        return stack.is(PrimitiveTags.CHOPPING_AXES) && !stack.is(PrimitiveTags.INVALID_CHOPPING_AXES);
    }

    public static Ingredient displayIngredient() {
        return Ingredient.of(PrimitiveTags.CHOPPING_AXES);
    }

    public static int tier(ItemStack stack) {
        if (!(stack.getItem() instanceof TieredItem tiered)) {
            return 0;
        }
        float speed = tiered.getTier().getSpeed();
        if (speed <= 2.0F) {
            return 0;
        }
        if (speed <= 4.0F) {
            return 1;
        }
        if (speed <= 6.0F) {
            return 2;
        }
        return 3;
    }

    public static int requiredChops(ChoppingRecipe recipe, int tier) {
        return recipe.chopsForTier(tier, defaultChops(tier));
    }

    public static int outputQuantity(ChoppingRecipe recipe, int tier) {
        return recipe.quantityForTier(tier, defaultQuantity(tier));
    }

    private static int defaultChops(int tier) {
        return switch (tier) {
            case 0 -> PrimitiveTechnologyConfig.CHOPPING_WOOD_CHOPS.get();
            case 1 -> PrimitiveTechnologyConfig.CHOPPING_STONE_CHOPS.get();
            case 2 -> PrimitiveTechnologyConfig.CHOPPING_IRON_CHOPS.get();
            default -> PrimitiveTechnologyConfig.CHOPPING_DIAMOND_CHOPS.get();
        };
    }

    private static int defaultQuantity(int tier) {
        return switch (tier) {
            case 0 -> PrimitiveTechnologyConfig.CHOPPING_WOOD_OUTPUT.get();
            case 1 -> PrimitiveTechnologyConfig.CHOPPING_STONE_OUTPUT.get();
            case 2 -> PrimitiveTechnologyConfig.CHOPPING_IRON_OUTPUT.get();
            default -> PrimitiveTechnologyConfig.CHOPPING_DIAMOND_OUTPUT.get();
        };
    }
}
