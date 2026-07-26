package com.protyvkultury.revivalages.api.food;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class FreshnessMathTest {

    @Test
    void calculatesLifetimeFromDecaySpeed() {
        assertEquals(316_800L, FreshnessMath.lifetime(633_600L, 2.0D));
        assertEquals(1_267_200L, FreshnessMath.lifetime(633_600L, 0.5D));
    }

    @Test
    void preservationCanMoveCreationBeforeClockOrigin() {
        assertEquals(-100L, FreshnessMath.creationForPreservedFraction(100L, 0L, 2.0D));
        assertEquals(50L, FreshnessMath.creationForPreservedFraction(100L, 0L, 0.5D));
    }
}
