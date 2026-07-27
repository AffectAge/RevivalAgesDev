package com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TanningRackBlockEntityTest {

    @Test
    void rainExposureAccumulatesAcrossDryIntervals() {
        int afterFirstStorm = TanningRackRainExposure.next(0, 100, true, true);
        int afterDryInterval = TanningRackRainExposure.next(afterFirstStorm, 100, false, true);
        int afterSecondStorm = TanningRackRainExposure.next(afterDryInterval, 100, true, true);

        assertEquals(1, afterFirstStorm);
        assertEquals(1, afterDryInterval);
        assertEquals(2, afterSecondStorm);
    }

    @Test
    void recipesWithoutRainFailureDoNotAccumulateExposure() {
        assertEquals(7, TanningRackRainExposure.next(7, 100, true, false));
    }

    @Test
    void disabledRainFailureAndReachedLimitKeepCurrentExposure() {
        assertEquals(7, TanningRackRainExposure.next(7, -1, true, true));
        assertEquals(100, TanningRackRainExposure.next(100, 100, true, true));
    }
}
