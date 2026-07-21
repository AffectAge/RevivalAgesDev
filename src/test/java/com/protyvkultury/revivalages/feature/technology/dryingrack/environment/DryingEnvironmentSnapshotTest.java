package com.protyvkultury.revivalages.feature.technology.dryingrack.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

final class DryingEnvironmentSnapshotTest {

    @Test
    void appliesAdditiveModifiersBeforeRackMultiplier() {
        DryingEnvironmentSnapshot snapshot = new DryingEnvironmentSnapshot(
                DryingEnvironmentBase.DEFAULT,
                1.0D,
                List.of(
                        new DryingEnvironmentModifier(DryingEnvironmentModifierType.HOT, 0.2D),
                        new DryingEnvironmentModifier(DryingEnvironmentModifierType.WINTER, -0.3D)
                ),
                1.35D
        );

        assertEquals(1.215D, snapshot.speed(), 1.0E-12D);
    }

    @Test
    void preservesNegativeConfiguredModifierValues() {
        DryingEnvironmentSnapshot snapshot = new DryingEnvironmentSnapshot(
                DryingEnvironmentBase.DEFAULT,
                0.1D,
                List.of(new DryingEnvironmentModifier(DryingEnvironmentModifierType.WINTER, -0.3D)),
                1.0D
        );

        assertEquals(-0.2D, snapshot.speed(), 1.0E-12D);
    }
}
