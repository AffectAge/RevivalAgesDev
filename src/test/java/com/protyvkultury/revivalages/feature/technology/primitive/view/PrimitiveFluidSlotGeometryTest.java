package com.protyvkultury.revivalages.feature.technology.primitive.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PrimitiveFluidSlotGeometryTest {

    @Test
    void emiOuterBoundsProduceTheSameVisibleFillAsJei() {
        assertGeometry(PrimitiveFluidSlotGeometry.BARREL_INPUT, 65, 21, 16, 16);
        assertGeometry(PrimitiveFluidSlotGeometry.BARREL_OUTPUT, 121, 21, 16, 16);
        assertGeometry(PrimitiveFluidSlotGeometry.SOAKING_POT_INPUT, 17, 30, 16, 16);
        assertGeometry(PrimitiveFluidSlotGeometry.STONE_CRUCIBLE_OUTPUT, 73, 11, 16, 16);
        assertGeometry(PrimitiveFluidSlotGeometry.PRESSING_OUTPUT, 97, 16, 16, 16);
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
