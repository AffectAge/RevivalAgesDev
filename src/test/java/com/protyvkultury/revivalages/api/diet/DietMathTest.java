package com.protyvkultury.revivalages.api.diet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class DietMathTest {

    @Test
    void appliesMultiGroupReductionToGain() {
        assertEquals(2.75D, DietMath.gain(10.0D, 0.5D, 1.0D, 0.15D, 4), 0.00001D);
    }

    @Test
    void decayUsesFoodLevelsAndGroupMultiplier() {
        assertEquals(49.7D, DietMath.decay(50.0D, 2, 0.075D, 2.0D), 0.00001D);
        assertEquals(30.0D, DietMath.deathPenalty(35.0D, 15.0D, 30.0D), 0.00001D);
    }
}
