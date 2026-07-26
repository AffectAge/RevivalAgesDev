package com.protyvkultury.revivalages.feature.player.diet;

public final class DietSettings {

    private static volatile Snapshot remote;

    private DietSettings() {
    }

    public static boolean enabled() {
        Snapshot snapshot = remote;
        return snapshot == null ? DietConfig.configuredEnabled() : snapshot.enabled();
    }

    public static double nutritionMultiplier() {
        Snapshot snapshot = remote;
        return snapshot == null ? value(DietConfig.NUTRITION_MULTIPLIER) : snapshot.nutritionMultiplier();
    }

    public static double multiGroupReduction() {
        Snapshot snapshot = remote;
        return snapshot == null ? value(DietConfig.MULTI_GROUP_REDUCTION) : snapshot.multiGroupReduction();
    }

    public static double milkNutrition() {
        Snapshot snapshot = remote;
        return snapshot == null ? value(DietConfig.MILK_NUTRITION) : snapshot.milkNutrition();
    }

    public static double cakeSliceNutrition() {
        Snapshot snapshot = remote;
        return snapshot == null ? value(DietConfig.CAKE_SLICE_NUTRITION) : snapshot.cakeSliceNutrition();
    }

    public static void acceptRemote(Snapshot snapshot) {
        remote = snapshot;
    }

    public static void clearRemote() {
        remote = null;
    }

    public static Snapshot localSnapshot() {
        return new Snapshot(
                DietConfig.configuredEnabled(),
                value(DietConfig.NUTRITION_MULTIPLIER),
                value(DietConfig.MULTI_GROUP_REDUCTION),
                value(DietConfig.MILK_NUTRITION),
                value(DietConfig.CAKE_SLICE_NUTRITION)
        );
    }

    private static double value(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue setting) {
        return DietConfig.SPEC.isLoaded() ? setting.get() : setting.getDefault();
    }

    public record Snapshot(
            boolean enabled,
            double nutritionMultiplier,
            double multiGroupReduction,
            double milkNutrition,
            double cakeSliceNutrition
    ) {

        public Snapshot {
            nutritionMultiplier = bounded(nutritionMultiplier, 0.5D);
            multiGroupReduction = Math.clamp(bounded(multiGroupReduction, 0.15D), 0.0D, 1.0D);
            milkNutrition = bounded(milkNutrition, 1.0D);
            cakeSliceNutrition = bounded(cakeSliceNutrition, 2.0D);
        }

        private static double bounded(double value, double fallback) {
            return Double.isFinite(value) && value >= 0.0D ? value : fallback;
        }
    }
}
