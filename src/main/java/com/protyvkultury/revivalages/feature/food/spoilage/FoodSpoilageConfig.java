package com.protyvkultury.revivalages.feature.food.spoilage;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FoodSpoilageConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.LongValue BASE_LIFETIME_TICKS;
    public static final ModConfigSpec.DoubleValue GLOBAL_DECAY_MULTIPLIER;
    public static final ModConfigSpec.LongValue STACKING_WINDOW_TICKS;
    public static final ModConfigSpec.BooleanValue AGE_THROUGH_SLEEP;
    public static final ModConfigSpec.DoubleValue DRIED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue PRESERVED_MULTIPLIER;
    public static final ModConfigSpec.IntValue MATERIALIZATION_CADENCE;
    public static final ModConfigSpec.IntValue MAXIMUM_CONTAINER_DEPTH;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("foodSpoilage");
        ENABLED = builder
                .comment("Enables the independent food spoilage clock and food decay.")
                .define("enabled", true);
        BASE_LIFETIME_TICKS = builder
                .comment("Base lifetime before an item's decay modifier is applied.")
                .defineInRange("baseLifetimeTicks", 22L * 28_800L, 1L, Long.MAX_VALUE);
        GLOBAL_DECAY_MULTIPLIER = builder
                .comment("Global decay speed. Larger values spoil food faster.")
                .defineInRange("globalDecayMultiplier", 1.0D, 0.0001D, 10_000.0D);
        STACKING_WINDOW_TICKS = builder
                .comment("Maximum creation-time difference accepted when perishable stacks merge.")
                .defineInRange("stackingWindowTicks", 6L * 1_200L, 0L, Long.MAX_VALUE);
        AGE_THROUGH_SLEEP = builder
                .comment("Adds successfully skipped Overworld night time to the spoilage clock.")
                .define("ageThroughSleep", true);
        DRIED_MULTIPLIER = builder
                .comment("Decay-speed multiplier for the dried trait.")
                .defineInRange("traits.driedMultiplier", 0.5D, 0.0001D, 10_000.0D);
        PRESERVED_MULTIPLIER = builder
                .comment("Decay-speed multiplier while food is preserved in a sealed container.")
                .defineInRange("traits.preservedMultiplier", 0.5D, 0.0001D, 10_000.0D);
        MATERIALIZATION_CADENCE = builder
                .comment("Ticks between inventory and open-container expiry sweeps.")
                .defineInRange("materializationCadenceTicks", 20, 1, 1_200);
        MAXIMUM_CONTAINER_DEPTH = builder
                .comment("Maximum portable-container nesting depth examined for spoilage.")
                .defineInRange("maximumContainerDepth", 8, 0, 32);
        builder.pop();
        SPEC = builder.build();
    }

    private FoodSpoilageConfig() {
    }

    public static boolean configuredEnabled() {
        return SPEC.isLoaded() ? ENABLED.get() : ENABLED.getDefault();
    }
}
