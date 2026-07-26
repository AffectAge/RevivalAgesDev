package com.protyvkultury.revivalages.feature.food.spoilage;

public final class FoodSpoilageSettings {

    private static volatile Remote remote;

    private FoodSpoilageSettings() {
    }

    public static void acceptRemote(long ticks, boolean enabled, long baseLifetime, double multiplier) {
        remote = new Remote(
                Math.max(0L, ticks),
                enabled,
                Math.max(1L, baseLifetime),
                positive(multiplier)
        );
    }

    public static void clearRemote() {
        remote = null;
    }

    public static long remoteTicks() {
        Remote value = remote;
        return value == null ? 0L : value.ticks();
    }

    public static boolean remoteEnabled() {
        Remote value = remote;
        return value == null ? FoodSpoilageConfig.configuredEnabled() : value.enabled();
    }

    public static long baseLifetime() {
        Remote value = remote;
        return value == null
                ? (FoodSpoilageConfig.SPEC.isLoaded()
                        ? FoodSpoilageConfig.BASE_LIFETIME_TICKS.get()
                        : FoodSpoilageConfig.BASE_LIFETIME_TICKS.getDefault())
                : value.baseLifetime();
    }

    public static double globalMultiplier() {
        Remote value = remote;
        return value == null
                ? (FoodSpoilageConfig.SPEC.isLoaded()
                        ? FoodSpoilageConfig.GLOBAL_DECAY_MULTIPLIER.get()
                        : FoodSpoilageConfig.GLOBAL_DECAY_MULTIPLIER.getDefault())
                : value.globalMultiplier();
    }

    private static double positive(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 1.0D;
    }

    private record Remote(long ticks, boolean enabled, long baseLifetime, double globalMultiplier) {
    }
}
