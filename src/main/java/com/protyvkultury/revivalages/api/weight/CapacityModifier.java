package com.protyvkultury.revivalages.api.weight;

/**
 * Additive and multiplicative adjustment to a player's carrying capacity.
 */
public record CapacityModifier(double additive, double multiplier) {

    public static final CapacityModifier IDENTITY = new CapacityModifier(0.0D, 1.0D);

    public CapacityModifier {
        additive = Double.isFinite(additive) ? additive : 0.0D;
        multiplier = Double.isFinite(multiplier) && multiplier >= 0.0D ? multiplier : 1.0D;
    }

    public static CapacityModifier additive(double value) {
        return new CapacityModifier(value, 1.0D);
    }
}
