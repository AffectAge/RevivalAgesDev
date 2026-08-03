package com.protyvkultury.revivalages.core.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProcessRuleLayoutTest {

    @Test
    void wrapsAndCentersTheFinalPartialRow() {
        ProcessRuleLayout layout = ProcessRuleLayout.of(82, 11);

        assertEquals(4, layout.columns());
        assertEquals(3, layout.rows());
        assertEquals(54, layout.height());
        assertEquals(15, layout.x(8));
        assertEquals(51, layout.x(10));
        assertEquals(46, layout.y(10, 8));
    }

    @Test
    void emptyRulesReserveNoVerticalSpace() {
        ProcessRuleLayout layout = ProcessRuleLayout.of(82, 0);

        assertEquals(0, layout.rows());
        assertEquals(0, layout.height());
        assertTrue(layout.columns() > 0);
    }
}
