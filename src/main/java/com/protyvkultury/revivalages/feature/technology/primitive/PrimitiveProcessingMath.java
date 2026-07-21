package com.protyvkultury.revivalages.feature.technology.primitive;

import java.util.List;

/** Deterministic balance math shared by primitive machines and unit tests. */
public final class PrimitiveProcessingMath {

    private PrimitiveProcessingMath() {
    }

    public static int tierValue(List<Integer> values, int tier, int fallback, int minimum) {
        if (values.isEmpty()) {
            return Math.max(minimum, fallback);
        }
        int index = Math.min(Math.max(0, tier), values.size() - 1);
        return Math.max(minimum, values.get(index));
    }

    public static int scaledDuration(int baseTicks, double multiplier) {
        return Math.max(1, (int) Math.round(baseTicks * multiplier));
    }

    public static double progress(int elapsedTicks, int totalTicks) {
        return totalTicks <= 0 ? 0.0D : Math.clamp((double) elapsedTicks / totalTicks, 0.0D, 1.0D);
    }
}
