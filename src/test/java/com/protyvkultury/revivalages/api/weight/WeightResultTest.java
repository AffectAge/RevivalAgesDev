package com.protyvkultury.revivalages.api.weight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class WeightResultTest {

    @Test
    void normalizesInvalidProviderNumbers() {
        WeightResult result = new WeightResult(Double.NaN, -20.0D, Double.POSITIVE_INFINITY);

        assertEquals(WeightResult.ZERO, result);
    }

    @Test
    void combinesAndScalesAllWeightParts() {
        WeightResult first = WeightResult.container(150.0D, 50.0D, 200.0D);
        WeightResult second = WeightResult.of(25.0D);

        WeightResult result = first.add(second).multiply(3);

        assertEquals(525.0D, result.weight());
        assertEquals(225.0D, result.baseWeight());
        assertEquals(600.0D, result.contentsWeight());
        assertTrue(result.hasModifiedWeight());
    }

    @Test
    void nonPositiveStackQuantityContributesNothing() {
        WeightResult result = WeightResult.of(100.0D);

        assertEquals(WeightResult.ZERO, result.multiply(0));
        assertFalse(result.multiply(1).hasModifiedWeight());
    }
}
