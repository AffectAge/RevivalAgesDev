package com.protyvkultury.revivalages.api.food;

public final class FreshnessMath {

    private FreshnessMath() {
    }

    public static long lifetime(long baseTicks, double decaySpeed) {
        if (!Double.isFinite(decaySpeed) || decaySpeed <= 0.0D) {
            return Long.MAX_VALUE;
        }
        return Math.max(1L, (long) Math.floor(baseTicks / decaySpeed));
    }

    public static long creationForPreservedFraction(long now, long oldCreation, double proportion) {
        double adjusted = (1.0D - proportion) * now + proportion * oldCreation;
        if (adjusted <= Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        if (adjusted >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return (long) Math.floor(adjusted);
    }

    public static boolean mayStack(
            FoodState first,
            FoodState second,
            long windowTicks
    ) {
        if (!first.traits().equals(second.traits())) {
            return false;
        }
        long difference;
        try {
            difference = Math.abs(Math.subtractExact(first.creationTick(), second.creationTick()));
        } catch (ArithmeticException overflow) {
            return false;
        }
        return difference <= Math.max(0L, windowTicks);
    }
}
