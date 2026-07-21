package com.protyvkultury.revivalages.feature.technology.dryingrack.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class SeasonalBonusesTest {

    @Test
    void returnsConfiguredValueForEverySeason() {
        SeasonalBonuses bonuses = new SeasonalBonuses(true, 0.1D, 0.3D, -0.1D, -0.3D);

        assertEquals(0.1D, bonuses.valueFor(SeasonType.SPRING));
        assertEquals(0.3D, bonuses.valueFor(SeasonType.SUMMER));
        assertEquals(-0.1D, bonuses.valueFor(SeasonType.AUTUMN));
        assertEquals(-0.3D, bonuses.valueFor(SeasonType.WINTER));
        assertEquals(0.0D, bonuses.valueFor(SeasonType.NONE));
    }

    @Test
    void disabledConfigurationAlwaysReturnsZero() {
        SeasonalBonuses bonuses = new SeasonalBonuses(false, 10.0D, 20.0D, -10.0D, -20.0D);

        for (SeasonType season : SeasonType.values()) {
            assertEquals(0.0D, bonuses.valueFor(season));
        }
    }

    @Test
    void acceptsPositiveZeroAndNegativeValues() {
        SeasonalBonuses bonuses = new SeasonalBonuses(true, -2.5D, 0.0D, 4.75D, -0.0D);

        assertEquals(-2.5D, bonuses.valueFor(SeasonType.SPRING));
        assertEquals(0.0D, bonuses.valueFor(SeasonType.SUMMER));
        assertEquals(4.75D, bonuses.valueFor(SeasonType.AUTUMN));
        assertEquals(-0.0D, bonuses.valueFor(SeasonType.WINTER));
    }
}
