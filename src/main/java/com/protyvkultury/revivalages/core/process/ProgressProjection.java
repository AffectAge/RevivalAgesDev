package com.protyvkultury.revivalages.core.process;

/** Projects a synchronized process counter between block-entity updates. */
public final class ProgressProjection {

    private ProgressProjection() {
    }

    public static double fraction(double elapsed, double duration, double rate, long snapshotTime, long gameTime) {
        if (duration <= 0.0D) {
            return 0.0D;
        }
        double projected = elapsed + Math.max(0L, gameTime - snapshotTime) * Math.max(0.0D, rate);
        return Math.clamp(projected / duration, 0.0D, 1.0D);
    }
}
