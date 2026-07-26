package com.protyvkultury.revivalages.api.diet;

import java.util.List;

public final class DietMath {

    private DietMath() {
    }

    public static double gain(
            double nutrition,
            double nutritionMultiplier,
            double itemMultiplier,
            double multiGroupReduction,
            int groupCount
    ) {
        double reduction = Math.max(0.0D, 1.0D
                - multiGroupReduction * Math.max(0, groupCount - 1));
        return Math.max(0.0D, nutrition * nutritionMultiplier * itemMultiplier * reduction);
    }

    public static double decay(double value, int lostFoodLevels, double rate, double groupMultiplier) {
        return Math.max(0.0D, value - Math.max(0, lostFoodLevels) * rate * groupMultiplier);
    }

    public static double deathPenalty(double value, double penalty, double minimum) {
        return Math.max(minimum, value - penalty);
    }

    public static boolean matches(
            DietDetector detector,
            List<Double> values,
            double minimum,
            double maximum
    ) {
        if (values.isEmpty()) {
            return false;
        }
        return switch (detector) {
            case ANY, CUMULATIVE -> values.stream().anyMatch(value -> within(value, minimum, maximum));
            case ALL -> values.stream().allMatch(value -> within(value, minimum, maximum));
            case AVERAGE -> within(
                    values.stream().mapToDouble(Double::doubleValue).average().orElseThrow(),
                    minimum,
                    maximum
            );
        };
    }

    private static boolean within(double value, double minimum, double maximum) {
        return value >= minimum && value <= maximum;
    }
}
