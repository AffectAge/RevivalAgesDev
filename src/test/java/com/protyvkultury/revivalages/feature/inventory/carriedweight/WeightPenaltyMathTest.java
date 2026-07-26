package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class WeightPenaltyMathTest {

    private static final WeightPenaltySettings SETTINGS = new WeightPenaltySettings(
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
    );

    @Test
    void everyAdditionalTenPercentRaisesOverloadLevel() {
        assertEquals(1, WeightPenaltyMath.overloadLevel(100.0D, 100.0D, 0, 0, SETTINGS));
        assertEquals(2, WeightPenaltyMath.overloadLevel(110.0D, 100.0D, 0, 0, SETTINGS));
        assertEquals(10, WeightPenaltyMath.overloadLevel(1_000.0D, 100.0D, 0, 0, SETTINGS));
    }

    @Test
    void strengthAndHasteReduceButNeverRemoveOverload() {
        assertEquals(4, WeightPenaltyMath.overloadLevel(150.0D, 100.0D, 1, 1, SETTINGS));
        assertEquals(1, WeightPenaltyMath.overloadLevel(110.0D, 100.0D, 10, 10, SETTINGS));
    }

    @Test
    void overloadPenaltiesAreBounded() {
        WeightPenaltyMath.Penalties first = WeightPenaltyMath.overloadPenalties(1, SETTINGS);
        WeightPenaltyMath.Penalties maximum = WeightPenaltyMath.overloadPenalties(10, SETTINGS);

        assertEquals(0.5D, first.movementSpeed());
        assertEquals(0.25D, first.attackDamage());
        assertTrue(maximum.movementSpeed() <= SETTINGS.maximumAttributePenalty());
        assertTrue(maximum.attackSpeed() <= SETTINGS.maximumAttributePenalty());
        assertTrue(maximum.attackDamage() <= SETTINGS.maximumAttributePenalty());
    }

    @Test
    void jumpPenaltyRespectsConfiguredFloor() {
        double jump = WeightPenaltyMath.jumpVelocity(0.42D, 200.0D, 100.0D, 2, SETTINGS);

        assertEquals(0.126D, jump, 0.000_001D);
    }
}
