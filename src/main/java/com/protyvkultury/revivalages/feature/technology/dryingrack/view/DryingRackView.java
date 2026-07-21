package com.protyvkultury.revivalages.feature.technology.dryingrack.view;

import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingEnvironmentSnapshot;
import java.util.List;

/**
 * Canonical read model shared by optional inspection integrations.
 */
public record DryingRackView(List<DryingSlotView> slots, DryingEnvironmentSnapshot environment) {

    public DryingRackView {
        slots = List.copyOf(slots);
    }

    public double speed() {
        return environment.speed();
    }
}
