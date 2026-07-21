package com.protyvkultury.revivalages.feature.technology.dryingrack.environment;

import java.util.List;

/**
 * Immutable explanation of the current Drying Rack speed calculation.
 */
public record DryingEnvironmentSnapshot(
        DryingEnvironmentBase base,
        double baseSpeed,
        List<DryingEnvironmentModifier> modifiers,
        double rackMultiplier
) {

    public static final DryingEnvironmentSnapshot EMPTY = new DryingEnvironmentSnapshot(
            DryingEnvironmentBase.DEFAULT,
            0.0D,
            List.of(),
            1.0D
    );

    public DryingEnvironmentSnapshot {
        modifiers = List.copyOf(modifiers);
    }

    public double speed() {
        double additiveSpeed = baseSpeed;
        for (DryingEnvironmentModifier modifier : modifiers) {
            additiveSpeed += modifier.amount();
        }
        return additiveSpeed * rackMultiplier;
    }
}
