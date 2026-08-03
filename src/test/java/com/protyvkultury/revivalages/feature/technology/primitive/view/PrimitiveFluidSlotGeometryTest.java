package com.protyvkultury.revivalages.feature.technology.primitive.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PrimitiveFluidSlotGeometryTest {

    @Test
    void emiOuterBoundsProduceTheSameVisibleFillAsJei() {
        assertGeometry(PrimitiveFluidSlotGeometry.BARREL_INPUT, 2, 40, 33, 9);
        assertGeometry(PrimitiveFluidSlotGeometry.BARREL_OUTPUT, 73, 2, 22, 47);
        assertGeometry(PrimitiveFluidSlotGeometry.SOAKING_POT_INPUT, 2, 21, 14, 14);
        assertGeometry(PrimitiveFluidSlotGeometry.STONE_CRUCIBLE_OUTPUT, 62, 12, 14, 14);
        assertGeometry(PrimitiveFluidSlotGeometry.PRESSING_OUTPUT, 117, 24, 14, 25);
    }

    private static void assertGeometry(PrimitiveFluidSlotGeometry geometry, int x, int y, int width, int height) {
        assertEquals(x, geometry.contentX());
        assertEquals(y, geometry.contentY());
        assertEquals(width, geometry.contentWidth());
        assertEquals(height, geometry.contentHeight());

        PrimitiveFluidSlotGeometry.EmiTankBounds emi = geometry.emiTankBounds();
        assertEquals(x, emi.x() + 1);
        assertEquals(y, emi.y() + 1);
        assertEquals(width, emi.width() - 2);
        assertEquals(height, emi.height() - 2);
    }
}
