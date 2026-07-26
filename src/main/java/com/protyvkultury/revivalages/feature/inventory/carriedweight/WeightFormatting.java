package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import java.util.Locale;

public final class WeightFormatting {

    private WeightFormatting() {
    }

    public static String compact(double grams) {
        if (grams >= 1_000_000_000.0D) {
            return String.format(Locale.ROOT, "%.1fB", grams / 1_000_000_000.0D);
        }
        if (grams >= 1_000_000.0D) {
            return String.format(Locale.ROOT, "%.1fM", grams / 1_000_000.0D);
        }
        if (grams >= 1_000.0D) {
            return String.format(Locale.ROOT, "%.1fk", grams / 1_000.0D);
        }
        return Long.toString((long) grams);
    }

    public static String exact(double grams) {
        return String.format(Locale.ROOT, "%,.0f", grams);
    }
}
