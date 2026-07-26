package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CarriedWeightSettings {

    private static volatile Snapshot local = Snapshot.defaults();
    private static volatile Snapshot remote;

    private CarriedWeightSettings() {
    }

    public static Snapshot snapshot() {
        Snapshot remoteSnapshot = remote;
        return remoteSnapshot == null ? local : remoteSnapshot;
    }

    public static boolean enabled() {
        return snapshot().enabled();
    }

    public static Snapshot refreshLocal() {
        Snapshot refreshed = fromConfig();
        local = refreshed;
        return refreshed;
    }

    public static void acceptRemote(Snapshot snapshot) {
        remote = snapshot;
    }

    public static void clearRemote() {
        remote = null;
    }

    private static Snapshot fromConfig() {
        return new Snapshot(
                ContentAvailability.isEnabled(ContentKey.CARRIED_WEIGHT),
                integer(CarriedWeightConfig.UPDATE_INTERVAL_TICKS),
                integer(CarriedWeightConfig.MAXIMUM_RECURSION_DEPTH),
                decimal(CarriedWeightConfig.BASE_CAPACITY),
                decimal(CarriedWeightConfig.POCKET_CAPACITY),
                new WeightFormulaSettings(
                        decimal(CarriedWeightConfig.BUCKET_WEIGHT),
                        decimal(CarriedWeightConfig.BOTTLE_WEIGHT),
                        decimal(CarriedWeightConfig.BLOCK_WEIGHT),
                        decimal(CarriedWeightConfig.INGOT_WEIGHT),
                        decimal(CarriedWeightConfig.NUGGET_WEIGHT),
                        decimal(CarriedWeightConfig.ITEM_WEIGHT),
                        decimal(CarriedWeightConfig.TECHNICAL_WEIGHT),
                        decimal(CarriedWeightConfig.CONTAINER_CONTENTS_MULTIPLIER),
                        decimal(CarriedWeightConfig.STACK_MULTIPLIER_COEFFICIENT),
                        decimal(CarriedWeightConfig.COMMON_RARITY_MULTIPLIER),
                        decimal(CarriedWeightConfig.UNCOMMON_RARITY_MULTIPLIER),
                        decimal(CarriedWeightConfig.RARE_RARITY_MULTIPLIER),
                        decimal(CarriedWeightConfig.EPIC_RARITY_MULTIPLIER),
                        decimal(CarriedWeightConfig.FAST_FOOD_THRESHOLD_SECONDS),
                        decimal(CarriedWeightConfig.FIRE_RESISTANT_MULTIPLIER),
                        decimal(CarriedWeightConfig.TOOL_DURABILITY_DIVISOR),
                        decimal(CarriedWeightConfig.TOOL_DURABILITY_WEIGHT),
                        decimal(CarriedWeightConfig.ARMOR_DURABILITY_DIVISOR),
                        decimal(CarriedWeightConfig.ARMOR_DURABILITY_WEIGHT),
                        decimal(CarriedWeightConfig.ARMOR_PROTECTION_WEIGHT),
                        decimal(CarriedWeightConfig.BLOCK_HARDNESS_WEIGHT),
                        decimal(CarriedWeightConfig.BLOCK_RESISTANCE_WEIGHT),
                        decimal(CarriedWeightConfig.BLOCK_RESISTANCE_WEIGHT_CAP),
                        decimal(CarriedWeightConfig.TRANSPARENT_BLOCK_MULTIPLIER),
                        decimal(CarriedWeightConfig.BLOCK_ENTITY_WEIGHT),
                        decimal(CarriedWeightConfig.SLAB_MULTIPLIER),
                        decimal(CarriedWeightConfig.STAIRS_MULTIPLIER)
                ),
                new WeightPenaltySettings(
                        bool(CarriedWeightConfig.REALISTIC_MODE),
                        decimal(CarriedWeightConfig.PENALTY_STRENGTH),
                        integer(CarriedWeightConfig.OVERLOAD_STEP_PERCENT),
                        integer(CarriedWeightConfig.MAXIMUM_OVERLOAD_LEVEL),
                        integer(CarriedWeightConfig.EFFECT_DURATION_TICKS),
                        decimal(CarriedWeightConfig.OVERLOAD_BASE_PENALTY),
                        decimal(CarriedWeightConfig.OVERLOAD_DAMAGE_BASE_PENALTY),
                        decimal(CarriedWeightConfig.OVERLOAD_LEVEL_PENALTY),
                        decimal(CarriedWeightConfig.MAXIMUM_ATTRIBUTE_PENALTY),
                        decimal(CarriedWeightConfig.REALISTIC_START_FRACTION),
                        decimal(CarriedWeightConfig.REALISTIC_SPEED_RELIEF),
                        decimal(CarriedWeightConfig.REALISTIC_ATTACK_SPEED_RELIEF),
                        decimal(CarriedWeightConfig.REALISTIC_DAMAGE_RELIEF),
                        decimal(CarriedWeightConfig.OVERLOAD_JUMP_MULTIPLIER),
                        decimal(CarriedWeightConfig.OVERLOAD_MINIMUM_JUMP_MULTIPLIER),
                        decimal(CarriedWeightConfig.REALISTIC_MINIMUM_JUMP_MULTIPLIER),
                        decimal(CarriedWeightConfig.REALISTIC_JUMP_BONUS)
                )
        );
    }

    private static boolean bool(ModConfigSpec.BooleanValue value) {
        return CarriedWeightConfig.SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private static int integer(ModConfigSpec.IntValue value) {
        return CarriedWeightConfig.SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private static double decimal(ModConfigSpec.DoubleValue value) {
        return CarriedWeightConfig.SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public record Snapshot(
            boolean enabled,
            int updateIntervalTicks,
            int maximumRecursionDepth,
            double baseCapacity,
            double pocketCapacity,
            WeightFormulaSettings formula,
            WeightPenaltySettings penalties
    ) {

        public Snapshot {
            updateIntervalTicks = Math.max(1, updateIntervalTicks);
            maximumRecursionDepth = Math.max(0, maximumRecursionDepth);
            baseCapacity = positive(baseCapacity, 90_000.0D);
            pocketCapacity = positive(pocketCapacity, 9_000.0D);
        }

        public static Snapshot defaults() {
            return new Snapshot(
                    true,
                    1,
                    8,
                    90_000.0D,
                    9_000.0D,
                    new WeightFormulaSettings(
                            120.0D,
                            60.0D,
                            240.0D,
                            90.0D,
                            10.0D,
                            50.0D,
                            30_000.0D,
                            0.5D,
                            10.0D,
                            1.3D,
                            1.95D,
                            2.6D,
                            3.9D,
                            0.8D,
                            1.25D,
                            1_500.0D,
                            300.0D,
                            300.0D,
                            300.0D,
                            10.0D,
                            10.0D,
                            50.0D,
                            10_000.0D,
                            0.8D,
                            50.0D,
                            0.5D,
                            0.875D
                    ),
                    new WeightPenaltySettings(
                            false,
                            1.0D,
                            10,
                            10,
                            40,
                            0.5D,
                            0.25D,
                            0.05D,
                            0.9D,
                            0.1D,
                            0.4D,
                            0.3D,
                            0.3D,
                            0.6D,
                            0.2D,
                            0.3D,
                            0.05D
                    )
            );
        }

        private static double positive(double value, double fallback) {
            return Double.isFinite(value) && value > 0.0D ? value : fallback;
        }
    }
}
