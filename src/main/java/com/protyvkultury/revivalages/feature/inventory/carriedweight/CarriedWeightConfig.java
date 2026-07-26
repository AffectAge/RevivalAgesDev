package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CarriedWeightConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue UPDATE_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue MAXIMUM_RECURSION_DEPTH;
    public static final ModConfigSpec.DoubleValue BASE_CAPACITY;
    public static final ModConfigSpec.DoubleValue POCKET_CAPACITY;
    public static final ModConfigSpec.DoubleValue BUCKET_WEIGHT;
    public static final ModConfigSpec.DoubleValue BOTTLE_WEIGHT;
    public static final ModConfigSpec.DoubleValue BLOCK_WEIGHT;
    public static final ModConfigSpec.DoubleValue INGOT_WEIGHT;
    public static final ModConfigSpec.DoubleValue NUGGET_WEIGHT;
    public static final ModConfigSpec.DoubleValue ITEM_WEIGHT;
    public static final ModConfigSpec.DoubleValue TECHNICAL_WEIGHT;
    public static final ModConfigSpec.DoubleValue CONTAINER_CONTENTS_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue STACK_MULTIPLIER_COEFFICIENT;
    public static final ModConfigSpec.DoubleValue COMMON_RARITY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue UNCOMMON_RARITY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue RARE_RARITY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue EPIC_RARITY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FAST_FOOD_THRESHOLD_SECONDS;
    public static final ModConfigSpec.DoubleValue FIRE_RESISTANT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue TOOL_DURABILITY_DIVISOR;
    public static final ModConfigSpec.DoubleValue TOOL_DURABILITY_WEIGHT;
    public static final ModConfigSpec.DoubleValue ARMOR_DURABILITY_DIVISOR;
    public static final ModConfigSpec.DoubleValue ARMOR_DURABILITY_WEIGHT;
    public static final ModConfigSpec.DoubleValue ARMOR_PROTECTION_WEIGHT;
    public static final ModConfigSpec.DoubleValue BLOCK_HARDNESS_WEIGHT;
    public static final ModConfigSpec.DoubleValue BLOCK_RESISTANCE_WEIGHT;
    public static final ModConfigSpec.DoubleValue BLOCK_RESISTANCE_WEIGHT_CAP;
    public static final ModConfigSpec.DoubleValue TRANSPARENT_BLOCK_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue BLOCK_ENTITY_WEIGHT;
    public static final ModConfigSpec.DoubleValue SLAB_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue STAIRS_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue REALISTIC_MODE;
    public static final ModConfigSpec.DoubleValue PENALTY_STRENGTH;
    public static final ModConfigSpec.IntValue OVERLOAD_STEP_PERCENT;
    public static final ModConfigSpec.IntValue MAXIMUM_OVERLOAD_LEVEL;
    public static final ModConfigSpec.IntValue EFFECT_DURATION_TICKS;
    public static final ModConfigSpec.DoubleValue OVERLOAD_BASE_PENALTY;
    public static final ModConfigSpec.DoubleValue OVERLOAD_DAMAGE_BASE_PENALTY;
    public static final ModConfigSpec.DoubleValue OVERLOAD_LEVEL_PENALTY;
    public static final ModConfigSpec.DoubleValue MAXIMUM_ATTRIBUTE_PENALTY;
    public static final ModConfigSpec.DoubleValue REALISTIC_START_FRACTION;
    public static final ModConfigSpec.DoubleValue REALISTIC_SPEED_RELIEF;
    public static final ModConfigSpec.DoubleValue REALISTIC_ATTACK_SPEED_RELIEF;
    public static final ModConfigSpec.DoubleValue REALISTIC_DAMAGE_RELIEF;
    public static final ModConfigSpec.DoubleValue OVERLOAD_JUMP_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERLOAD_MINIMUM_JUMP_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue REALISTIC_MINIMUM_JUMP_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue REALISTIC_JUMP_BONUS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("carriedWeight");
        ENABLED = builder
                .comment(
                        "Enables carried weight, pockets, HUD, tooltips, and overload penalties.",
                        "Changing this value requires a server restart."
                )
                .define("enabled", true);
        UPDATE_INTERVAL_TICKS = builder
                .comment("Ticks between authoritative player weight recalculations.")
                .defineInRange("updateIntervalTicks", 1, 1, 200);
        MAXIMUM_RECURSION_DEPTH = builder
                .comment("Maximum recursion depth for portable containers.")
                .defineInRange("maximumRecursionDepth", 8, 0, 32);
        BASE_CAPACITY = positive(builder, "capacity.base", 90_000.0D);
        POCKET_CAPACITY = positive(builder, "capacity.perPocket", 9_000.0D);

        BUCKET_WEIGHT = positive(builder, "weights.bucket", 120.0D);
        BOTTLE_WEIGHT = positive(builder, "weights.bottle", 60.0D);
        BLOCK_WEIGHT = positive(builder, "weights.block", 240.0D);
        INGOT_WEIGHT = positive(builder, "weights.ingotGemShard", 90.0D);
        NUGGET_WEIGHT = positive(builder, "weights.nugget", 10.0D);
        ITEM_WEIGHT = positive(builder, "weights.genericItem", 50.0D);
        TECHNICAL_WEIGHT = positive(builder, "weights.technicalItem", 30_000.0D);
        CONTAINER_CONTENTS_MULTIPLIER = nonNegative(
                builder,
                "weights.containerContentsMultiplier",
                0.5D,
                100.0D
        );
        STACK_MULTIPLIER_COEFFICIENT = nonNegative(
                builder,
                "formula.stackMultiplierCoefficient",
                10.0D,
                10_000.0D
        );
        COMMON_RARITY_MULTIPLIER = nonNegative(builder, "formula.rarity.common", 1.3D, 100.0D);
        UNCOMMON_RARITY_MULTIPLIER = nonNegative(builder, "formula.rarity.uncommon", 1.95D, 100.0D);
        RARE_RARITY_MULTIPLIER = nonNegative(builder, "formula.rarity.rare", 2.6D, 100.0D);
        EPIC_RARITY_MULTIPLIER = nonNegative(builder, "formula.rarity.epic", 3.9D, 100.0D);
        FAST_FOOD_THRESHOLD_SECONDS = nonNegative(builder, "formula.food.fastThresholdSeconds", 0.8D, 60.0D);
        FIRE_RESISTANT_MULTIPLIER = nonNegative(builder, "formula.fireResistantMultiplier", 1.25D, 100.0D);
        TOOL_DURABILITY_DIVISOR = positive(builder, "formula.toolDurabilityDivisor", 1_500.0D);
        TOOL_DURABILITY_WEIGHT = nonNegative(builder, "formula.toolDurabilityWeight", 300.0D, 1_000_000.0D);
        ARMOR_DURABILITY_DIVISOR = positive(builder, "formula.armorDurabilityDivisor", 300.0D);
        ARMOR_DURABILITY_WEIGHT = nonNegative(
                builder,
                "formula.armorDurabilityWeight",
                300.0D,
                1_000_000.0D
        );
        ARMOR_PROTECTION_WEIGHT = nonNegative(builder, "formula.armorProtectionWeight", 10.0D, 1_000_000.0D);
        BLOCK_HARDNESS_WEIGHT = nonNegative(builder, "formula.blockHardnessWeight", 10.0D, 1_000_000.0D);
        BLOCK_RESISTANCE_WEIGHT = nonNegative(builder, "formula.blockResistanceWeight", 50.0D, 1_000_000.0D);
        BLOCK_RESISTANCE_WEIGHT_CAP = nonNegative(
                builder,
                "formula.blockResistanceWeightCap",
                10_000.0D,
                1_000_000_000.0D
        );
        TRANSPARENT_BLOCK_MULTIPLIER = nonNegative(
                builder,
                "formula.transparentBlockMultiplier",
                0.8D,
                100.0D
        );
        BLOCK_ENTITY_WEIGHT = nonNegative(builder, "formula.blockEntityWeight", 50.0D, 1_000_000.0D);
        SLAB_MULTIPLIER = nonNegative(builder, "formula.slabMultiplier", 0.5D, 100.0D);
        STAIRS_MULTIPLIER = nonNegative(builder, "formula.stairsMultiplier", 0.875D, 100.0D);

        REALISTIC_MODE = builder
                .comment("Applies gradual penalties before full overload.")
                .define("penalties.realisticMode", false);
        PENALTY_STRENGTH = nonNegative(builder, "penalties.strength", 1.0D, 100.0D);
        OVERLOAD_STEP_PERCENT = builder
                .defineInRange("penalties.overloadStepPercent", 10, 1, 100);
        MAXIMUM_OVERLOAD_LEVEL = builder
                .defineInRange("penalties.maximumOverloadLevel", 10, 1, 255);
        EFFECT_DURATION_TICKS = builder
                .defineInRange("penalties.effectDurationTicks", 40, 1, 20_000);
        OVERLOAD_BASE_PENALTY = fraction(builder, "penalties.overloadBase", 0.5D);
        OVERLOAD_DAMAGE_BASE_PENALTY = fraction(builder, "penalties.overloadDamageBase", 0.25D);
        OVERLOAD_LEVEL_PENALTY = fraction(builder, "penalties.perLevel", 0.05D);
        MAXIMUM_ATTRIBUTE_PENALTY = fraction(builder, "penalties.maximumAttribute", 0.9D);
        REALISTIC_START_FRACTION = fraction(builder, "penalties.realisticStartFraction", 0.1D);
        REALISTIC_SPEED_RELIEF = fraction(builder, "penalties.realisticSpeedRelief", 0.4D);
        REALISTIC_ATTACK_SPEED_RELIEF = fraction(builder, "penalties.realisticAttackSpeedRelief", 0.3D);
        REALISTIC_DAMAGE_RELIEF = fraction(builder, "penalties.realisticDamageRelief", 0.3D);
        OVERLOAD_JUMP_MULTIPLIER = nonNegative(builder, "penalties.overloadJumpMultiplier", 0.6D, 10.0D);
        OVERLOAD_MINIMUM_JUMP_MULTIPLIER = fraction(
                builder,
                "penalties.overloadMinimumJumpMultiplier",
                0.2D
        );
        REALISTIC_MINIMUM_JUMP_MULTIPLIER = fraction(
                builder,
                "penalties.realisticMinimumJumpMultiplier",
                0.3D
        );
        REALISTIC_JUMP_BONUS = nonNegative(builder, "penalties.realisticJumpBonus", 0.05D, 10.0D);
        builder.pop();
        SPEC = builder.build();
    }

    private CarriedWeightConfig() {
    }

    public static boolean configuredEnabled() {
        return SPEC.isLoaded() ? ENABLED.get() : ENABLED.getDefault();
    }

    private static ModConfigSpec.DoubleValue positive(
            ModConfigSpec.Builder builder,
            String path,
            double defaultValue
    ) {
        return builder.defineInRange(path, defaultValue, Double.MIN_NORMAL, 1_000_000_000.0D);
    }

    private static ModConfigSpec.DoubleValue nonNegative(
            ModConfigSpec.Builder builder,
            String path,
            double defaultValue,
            double maximum
    ) {
        return builder.defineInRange(path, defaultValue, 0.0D, maximum);
    }

    private static ModConfigSpec.DoubleValue fraction(
            ModConfigSpec.Builder builder,
            String path,
            double defaultValue
    ) {
        return builder.defineInRange(path, defaultValue, 0.0D, 1.0D);
    }
}
