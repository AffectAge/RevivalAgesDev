package com.protyvkultury.revivalages.feature.technology.dryingrack.environment;

/**
 * Configurable seasonal balance snapshot used by the drying-speed formula.
 */
public record SeasonalBonuses(
        boolean enabled,
        double spring,
        double summer,
        double autumn,
        double winter
) {

    public double valueFor(SeasonType season) {
        if (!enabled) {
            return 0.0D;
        }
        return switch (season) {
            case SPRING -> spring;
            case SUMMER -> summer;
            case AUTUMN -> autumn;
            case WINTER -> winter;
            case NONE -> 0.0D;
        };
    }
}
