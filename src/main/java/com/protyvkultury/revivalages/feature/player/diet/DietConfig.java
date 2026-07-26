package com.protyvkultury.revivalages.feature.player.diet;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DietConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue STARTING_VALUE;
    public static final ModConfigSpec.DoubleValue NUTRITION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MILK_NUTRITION;
    public static final ModConfigSpec.DoubleValue CAKE_SLICE_NUTRITION;
    public static final ModConfigSpec.DoubleValue MULTI_GROUP_REDUCTION;
    public static final ModConfigSpec.DoubleValue HUNGER_DECAY;
    public static final ModConfigSpec.DoubleValue DEATH_PENALTY;
    public static final ModConfigSpec.DoubleValue DEATH_MINIMUM;
    public static final ModConfigSpec.IntValue EFFECT_CADENCE;
    public static final ModConfigSpec.IntValue EFFECT_DURATION;
    public static final ModConfigSpec.DoubleValue WEAKNESS_MAXIMUM;
    public static final ModConfigSpec.DoubleValue MINING_FATIGUE_MAXIMUM;
    public static final ModConfigSpec.DoubleValue RESISTANCE_MINIMUM;
    public static final ModConfigSpec.DoubleValue STRENGTH_MINIMUM;
    public static final ModConfigSpec.DoubleValue TOUGHNESS_MINIMUM;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("diet");
        ENABLED = builder
                .comment("Enables diet tracking, effects, tooltips, and the diet screen.")
                .define("enabled", true);
        STARTING_VALUE = value(builder, "startingValue", 50.0D, 0.0D, 100.0D);
        NUTRITION_MULTIPLIER = value(builder, "nutritionMultiplier", 0.5D, 0.0D, 100.0D);
        MILK_NUTRITION = value(builder, "milkNutrition", 1.0D, 0.0D, 100.0D);
        CAKE_SLICE_NUTRITION = value(builder, "cakeSliceNutrition", 2.0D, 0.0D, 100.0D);
        MULTI_GROUP_REDUCTION = value(builder, "multiGroupReduction", 0.15D, 0.0D, 1.0D);
        HUNGER_DECAY = value(builder, "hungerDecayPerPoint", 0.075D, 0.0D, 100.0D);
        DEATH_PENALTY = value(builder, "deathPenalty", 15.0D, 0.0D, 100.0D);
        DEATH_MINIMUM = value(builder, "deathMinimum", 30.0D, 0.0D, 100.0D);
        EFFECT_CADENCE = builder
                .comment("Ticks between authoritative diet effect recalculations.")
                .defineInRange("effectCadenceTicks", 110, 1, 20_000);
        EFFECT_DURATION = builder
                .comment("Duration assigned to refreshed diet effects.")
                .defineInRange("effectDurationTicks", 619, 1, 100_000);
        WEAKNESS_MAXIMUM = value(builder, "effects.weaknessMaximum", 10.0D, 0.0D, 100.0D);
        MINING_FATIGUE_MAXIMUM = value(builder, "effects.miningFatigueMaximum", 20.0D, 0.0D, 100.0D);
        RESISTANCE_MINIMUM = value(builder, "effects.resistanceMinimum", 80.0D, 0.0D, 100.0D);
        STRENGTH_MINIMUM = value(builder, "effects.strengthMinimum", 90.0D, 0.0D, 100.0D);
        TOUGHNESS_MINIMUM = value(builder, "effects.toughnessMinimum", 90.0D, 0.0D, 100.0D);
        builder.pop();
        SPEC = builder.build();
    }

    private DietConfig() {
    }

    public static boolean configuredEnabled() {
        return SPEC.isLoaded() ? ENABLED.get() : ENABLED.getDefault();
    }

    private static ModConfigSpec.DoubleValue value(
            ModConfigSpec.Builder builder,
            String path,
            double defaultValue,
            double minimum,
            double maximum
    ) {
        return builder.defineInRange(path, defaultValue, minimum, maximum);
    }
}
