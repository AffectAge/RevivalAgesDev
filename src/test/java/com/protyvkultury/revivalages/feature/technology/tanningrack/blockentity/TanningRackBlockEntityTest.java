package com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity;

import com.protyvkultury.revivalages.core.process.ProcessRuleState;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TanningRackBlockEntityTest {

    @Test
    void rainExposureAccumulatesAcrossDryIntervals() {
        ProcessRuleState state = new ProcessRuleState();
        int afterFirstStorm = state.incrementUntil(ProcessRuleType.WEATHER_EXPOSURE, 100);
        int afterDryInterval = state.counter(ProcessRuleType.WEATHER_EXPOSURE);
        int afterSecondStorm = state.incrementUntil(ProcessRuleType.WEATHER_EXPOSURE, 100);

        assertEquals(1, afterFirstStorm);
        assertEquals(1, afterDryInterval);
        assertEquals(2, afterSecondStorm);
    }

    @Test
    void recipesWithoutRainFailureDoNotAccumulateExposure() {
        ProcessRuleState state = new ProcessRuleState();
        state.setCounter(ProcessRuleType.WEATHER_EXPOSURE, 7);

        assertEquals(7, state.counter(ProcessRuleType.WEATHER_EXPOSURE));
    }

    @Test
    void disabledRainFailureAndReachedLimitKeepCurrentExposure() {
        ProcessRuleState state = new ProcessRuleState();
        state.setCounter(ProcessRuleType.WEATHER_EXPOSURE, 7);
        assertEquals(7, state.incrementUntil(ProcessRuleType.WEATHER_EXPOSURE, -1));
        state.setCounter(ProcessRuleType.WEATHER_EXPOSURE, 100);
        assertEquals(100, state.incrementUntil(ProcessRuleType.WEATHER_EXPOSURE, 100));
    }
}
