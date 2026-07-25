package com.protyvkultury.revivalages.feature.technology.knapping.recipe;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KnappingPatternTest {

    @Test
    void matchesCenteredPatternAndHorizontalMirror() {
        int centered = (1 << 11) | (1 << 12);
        int mirrored = (1 << 12) | (1 << 13);

        assertTrue(KnappingPatternMatcher.matches(centered, 3, 1, 0b011, false));
        assertTrue(KnappingPatternMatcher.matches(mirrored, 3, 1, 0b011, false));
    }

    @Test
    void acceptsVerticalOffset() {
        int shiftedRow = (1 << 6) | (1 << 7) | (1 << 8);

        assertTrue(KnappingPatternMatcher.matches(shiftedRow, 3, 1, 0b111, false));
    }

    @Test
    void honorsDefaultOutsideState() {
        int allExceptCenter = KnappingMenuBits.ALL & ~(1 << 12);

        assertTrue(KnappingPatternMatcher.matches(allExceptCenter, 1, 1, 0, true));
    }

    @Test
    void rejectsStateThatDoesNotMatchOutsideDefault() {
        int onlyTopLeft = 1;

        assertFalse(KnappingPatternMatcher.matches(onlyTopLeft, 1, 1, 1, true));
    }

    private static final class KnappingMenuBits {

        private static final int ALL = (1 << 25) - 1;

        private KnappingMenuBits() {
        }
    }
}
