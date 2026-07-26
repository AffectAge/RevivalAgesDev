package com.protyvkultury.revivalages.api.weight;

/**
 * Immutable per-unit weight calculation.
 *
 * @param weight effective weight in grams
 * @param baseWeight weight before dynamic container contents or other modifiers
 * @param contentsWeight unmodified weight of contained items
 */
public record WeightResult(double weight, double baseWeight, double contentsWeight) {

    public static final WeightResult ZERO = new WeightResult(0.0D, 0.0D, 0.0D);

    public WeightResult {
        weight = sanitize(weight);
        baseWeight = sanitize(baseWeight);
        contentsWeight = sanitize(contentsWeight);
    }

    public static WeightResult of(double weight) {
        return new WeightResult(weight, weight, 0.0D);
    }

    public static WeightResult container(double effectiveWeight, double emptyWeight, double contentsWeight) {
        return new WeightResult(effectiveWeight, emptyWeight, contentsWeight);
    }

    public WeightResult add(WeightResult other) {
        return new WeightResult(
                weight + other.weight,
                baseWeight + other.baseWeight,
                contentsWeight + other.contentsWeight
        );
    }

    public WeightResult multiply(int multiplier) {
        if (multiplier <= 0) {
            return ZERO;
        }
        return new WeightResult(
                weight * multiplier,
                baseWeight * multiplier,
                contentsWeight * multiplier
        );
    }

    public boolean hasModifiedWeight() {
        return Double.compare(weight, baseWeight) != 0;
    }

    /**
     * Reapplies the public numeric invariant to a provider result.
     */
    public WeightResult sanitized() {
        return new WeightResult(weight, baseWeight, contentsWeight);
    }

    private static double sanitize(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 0.0D;
    }
}
