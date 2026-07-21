package com.protyvkultury.revivalages.feature.technology.primitive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

final class PrimitiveProcessingMathTest {

    @Test
    void tierValueUsesFallbackAndMinimumWhenRecipeDoesNotOverrideTiers() {
        assertEquals(6, PrimitiveProcessingMath.tierValue(List.of(), 0, 6, 1));
        assertEquals(1, PrimitiveProcessingMath.tierValue(List.of(), 0, 0, 1));
    }

    @Test
    void tierValueClampsTierToAvailableRecipeOverrides() {
        List<Integer> values = List.of(8, 6, 4, 3);

        assertEquals(8, PrimitiveProcessingMath.tierValue(values, -1, 99, 1));
        assertEquals(4, PrimitiveProcessingMath.tierValue(values, 2, 99, 1));
        assertEquals(3, PrimitiveProcessingMath.tierValue(values, 20, 99, 1));
    }

    @Test
    void durationAndProgressStayWithinMachineInvariants() {
        assertEquals(1, PrimitiveProcessingMath.scaledDuration(1200, 0.0D));
        assertEquals(1800, PrimitiveProcessingMath.scaledDuration(1200, 1.5D));
        assertEquals(0.0D, PrimitiveProcessingMath.progress(10, 0));
        assertEquals(0.5D, PrimitiveProcessingMath.progress(50, 100));
        assertEquals(1.0D, PrimitiveProcessingMath.progress(150, 100));
    }
}
