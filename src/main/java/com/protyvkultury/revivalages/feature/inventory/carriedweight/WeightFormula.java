package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

final class WeightFormula {

    private WeightFormula() {
    }

    static double itemWeight(ItemStack stack, WeightFormulaSettings settings) {
        double weight = categoryWeight(stack, settings);
        int maximumStackSize = stack.getMaxStackSize();
        int maximumDamage = stack.getMaxDamage();

        if (maximumStackSize > 1) {
            weight *= 1.0D + settings.stackMultiplierCoefficient() / maximumStackSize;
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null) {
                double foodWeight = food.nutrition();
                if (food.eatSeconds() <= settings.fastFoodThresholdSeconds()) {
                    foodWeight *= 0.5D;
                }
                weight += foodWeight + food.saturation();
            }
            if (stack.has(DataComponents.FIRE_RESISTANT)) {
                weight *= settings.fireResistantMultiplier();
            }
        } else if (maximumDamage > 0) {
            if (stack.getItem() instanceof ArmorItem armor) {
                weight += armor.getDefense() * settings.armorProtectionWeight();
                weight += settings.itemWeight();
                weight += maximumDamage / settings.armorDurabilityDivisor()
                        * settings.armorDurabilityWeight();
            } else if (stack.getItem() instanceof TieredItem) {
                weight += settings.itemWeight();
                weight += maximumDamage / settings.toolDurabilityDivisor()
                        * settings.toolDurabilityWeight();
            }
        }

        return floor(weight * rarityMultiplier(stack.getRarity(), settings));
    }

    static double blockWeight(BlockItem blockItem, ItemStack stack, WeightFormulaSettings settings) {
        BlockState state = blockItem.getBlock().defaultBlockState();
        double base = stack.is(CarriedWeightTags.TECHNICAL_ITEMS)
                ? settings.technicalWeight()
                : settings.blockWeight();
        float destroySpeed = state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        base += Math.max(0.0D, destroySpeed) * settings.blockHardnessWeight();
        base += Math.min(
                blockItem.getBlock().getExplosionResistance(
                        state,
                        EmptyBlockGetter.INSTANCE,
                        BlockPos.ZERO,
                        null
                ) * settings.blockResistanceWeight(),
                settings.blockResistanceWeightCap()
        );
        if (!state.canOcclude()) {
            base *= settings.transparentBlockMultiplier();
        }
        if (blockItem.getBlock() instanceof EntityBlock) {
            base += settings.blockEntityWeight();
        }
        if (blockItem.getBlock() instanceof SlabBlock) {
            base *= settings.slabMultiplier();
        } else if (blockItem.getBlock() instanceof StairBlock) {
            base *= settings.stairsMultiplier();
        }
        return floor(base * rarityMultiplier(stack.getRarity(), settings));
    }

    static int armorPockets(ArmorItem armor) {
        return Math.max(1, 7 - (int) Math.floor(armor.getDefense() / 1.2D) - (int) armor.getToughness());
    }

    private static double categoryWeight(ItemStack stack, WeightFormulaSettings settings) {
        if (stack.is(CarriedWeightTags.TECHNICAL_ITEMS)) {
            return settings.technicalWeight();
        }
        if (stack.getItem() instanceof BucketItem || stack.is(CarriedWeightTags.BUCKETS)) {
            return settings.bucketWeight();
        }
        if (stack.getItem() instanceof PotionItem
                || stack.is(Items.GLASS_BOTTLE)
                || stack.is(CarriedWeightTags.BOTTLES)) {
            return settings.bottleWeight();
        }
        if (stack.is(CarriedWeightTags.INGOTS)
                || stack.is(CarriedWeightTags.GEMS)
                || stack.is(CarriedWeightTags.SHARDS)) {
            return settings.ingotWeight();
        }
        if (stack.is(CarriedWeightTags.NUGGETS)) {
            return settings.nuggetWeight();
        }
        return settings.itemWeight();
    }

    private static double rarityMultiplier(Rarity rarity, WeightFormulaSettings settings) {
        return switch (rarity) {
            case COMMON -> settings.commonRarityMultiplier();
            case UNCOMMON -> settings.uncommonRarityMultiplier();
            case RARE -> settings.rareRarityMultiplier();
            case EPIC -> settings.epicRarityMultiplier();
        };
    }

    private static double floor(double value) {
        if (!Double.isFinite(value)) {
            return 0.0D;
        }
        return Math.floor(Math.max(1.0D, value));
    }
}
