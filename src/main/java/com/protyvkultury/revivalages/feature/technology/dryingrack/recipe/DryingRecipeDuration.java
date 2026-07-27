package com.protyvkultury.revivalages.feature.technology.dryingrack.recipe;

import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackConfig;

public final class DryingRecipeDuration {

    private DryingRecipeDuration() {
    }

    public static int effective(int rawTicks, boolean normalRack, boolean inheritedCrude) {
        double baseModifier = normalRack
                ? DryingRackConfig.NORMAL_BASE_RECIPE_DURATION_MODIFIER.get()
                : DryingRackConfig.CRUDE_BASE_RECIPE_DURATION_MODIFIER.get();
        double inheritedModifier = inheritedCrude
                ? DryingRackConfig.INHERITED_CRUDE_RECIPE_DURATION_MODIFIER.get()
                : 1.0D;
        return effective(rawTicks, baseModifier, inheritedModifier);
    }

    static int effective(int rawTicks, double baseModifier, double inheritedModifier) {
        double duration = Math.max(1, rawTicks)
                * Math.max(0.01D, baseModifier)
                * Math.max(0.01D, inheritedModifier);
        return Math.max(1, (int) Math.round(Math.min(Integer.MAX_VALUE, duration)));
    }
}
