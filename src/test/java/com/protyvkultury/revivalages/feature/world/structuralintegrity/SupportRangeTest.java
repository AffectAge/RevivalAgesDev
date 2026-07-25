package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SupportRangeTest {

    @Test
    void acceptsConfiguredBoundaries() {
        SupportRange range = new SupportRange(2, 3, 4);

        assertTrue(range.contains(4, 2, -4));
        assertTrue(range.contains(-4, -3, 4));
    }

    @Test
    void rejectsPositionsOutsideAnyAxis() {
        SupportRange range = new SupportRange(2, 3, 4);

        assertFalse(range.contains(5, 0, 0));
        assertFalse(range.contains(0, 3, 0));
        assertFalse(range.contains(0, -4, 0));
    }

    @Test
    void treatsUpAndDownAsIndependentAsymmetricBounds() {
        SupportRange range = new SupportRange(1, 5, 0);

        assertTrue(range.contains(0, 1, 0));
        assertFalse(range.contains(0, 2, 0));
        assertTrue(range.contains(0, -5, 0));
        assertFalse(range.contains(0, -6, 0));
    }
}
