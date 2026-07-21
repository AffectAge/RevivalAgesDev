package com.protyvkultury.revivalages.core.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class BurnableStructureTrackerTest {

    @Test
    void invalidStructureReceivesGracePeriodAndCanRecover() {
        BurnableStructureTracker tracker = new BurnableStructureTracker(20, 3);

        assertEquals(BurnableStructureTracker.Result.INVALID_GRACE, tracker.tick(() -> false));
        assertEquals(BurnableStructureTracker.Result.INVALID_GRACE, tracker.tick(() -> false));
        assertEquals(BurnableStructureTracker.Result.VALID, tracker.tick(() -> true));
        assertEquals(0, tracker.invalidTicks());
    }

    @Test
    void invalidStructureFailsWhenGracePeriodExpires() {
        BurnableStructureTracker tracker = new BurnableStructureTracker(20, 3);

        tracker.tick(() -> false);
        tracker.tick(() -> false);
        tracker.tick(() -> false);

        assertEquals(BurnableStructureTracker.Result.FAILED, tracker.tick(() -> false));
    }

    @Test
    void validStructureIsNotRecheckedUntilIntervalExpires() {
        BurnableStructureTracker tracker = new BurnableStructureTracker(2, 3);

        assertEquals(BurnableStructureTracker.Result.VALID, tracker.tick(() -> true));
        assertEquals(BurnableStructureTracker.Result.VALID, tracker.tick(() -> false));
        assertEquals(BurnableStructureTracker.Result.INVALID_GRACE, tracker.tick(() -> false));
    }
}
