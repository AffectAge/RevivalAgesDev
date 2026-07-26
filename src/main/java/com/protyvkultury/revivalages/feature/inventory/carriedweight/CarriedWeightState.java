package com.protyvkultury.revivalages.feature.inventory.carriedweight;

public record CarriedWeightState(double currentWeight, double capacity, boolean overloaded) {

    public static final CarriedWeightState EMPTY = new CarriedWeightState(0.0D, 0.0D, false);

    public CarriedWeightState {
        currentWeight = sanitize(currentWeight);
        capacity = sanitize(capacity);
        overloaded = overloaded && capacity > 0.0D;
    }

    private static double sanitize(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 0.0D;
    }
}
