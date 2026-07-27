package com.protyvkultury.revivalages.feature.technology.dryingrack.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class DryingRecipeDurationTest {

    @Test
    void appliesRackAndInheritedModifiersInOrder() {
        assertEquals(14_400, DryingRecipeDuration.effective(14_400, 1.0D, 1.0D));
        assertEquals(7_200, DryingRecipeDuration.effective(14_400, 0.5D, 1.0D));
        assertEquals(21_600, DryingRecipeDuration.effective(14_400, 1.0D, 1.5D));
        assertEquals(10_800, DryingRecipeDuration.effective(14_400, 0.5D, 1.5D));
    }

    @Test
    void clampsInvalidEffectiveDurationToAtLeastOneTick() {
        assertEquals(1, DryingRecipeDuration.effective(0, 0.0D, 0.0D));
    }
}
