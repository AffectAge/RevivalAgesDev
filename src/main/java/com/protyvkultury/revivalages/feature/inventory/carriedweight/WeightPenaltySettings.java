package com.protyvkultury.revivalages.feature.inventory.carriedweight;

public record WeightPenaltySettings(
        boolean realisticMode,
        double strength,
        int overloadStepPercent,
        int maximumOverloadLevel,
        int effectDurationTicks,
        double overloadBasePenalty,
        double overloadDamageBasePenalty,
        double overloadLevelPenalty,
        double maximumAttributePenalty,
        double realisticStartFraction,
        double realisticSpeedRelief,
        double realisticAttackSpeedRelief,
        double realisticDamageRelief,
        double overloadJumpMultiplier,
        double overloadMinimumJumpMultiplier,
        double realisticMinimumJumpMultiplier,
        double realisticJumpBonus
) {
}
