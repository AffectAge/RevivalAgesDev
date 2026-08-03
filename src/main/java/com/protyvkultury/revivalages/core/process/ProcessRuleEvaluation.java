package com.protyvkultury.revivalages.core.process;

import java.util.List;

/** Result of applying all gate and modifier rules for one server process tick. */
public record ProcessRuleEvaluation(
        boolean canAdvance,
        boolean resetProgress,
        double speedMultiplier,
        List<ProcessRuleType> unmetGates) {

    public ProcessRuleEvaluation {
        speedMultiplier = Math.max(0.0D, speedMultiplier);
        unmetGates = List.copyOf(unmetGates);
    }
}
