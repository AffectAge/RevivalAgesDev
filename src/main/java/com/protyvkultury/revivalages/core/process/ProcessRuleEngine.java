package com.protyvkultury.revivalages.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/** Applies reusable gate and modifier semantics without any client dependency. */
public final class ProcessRuleEngine {

    private ProcessRuleEngine() {
    }

    public static ProcessRuleEvaluation evaluate(
            List<ProcessRule> rules,
            Predicate<ProcessRuleType> gateSatisfied,
            Function<ProcessRuleType, Double> modifier
    ) {
        boolean canAdvance = true;
        boolean resetProgress = false;
        double speedMultiplier = 1.0D;
        List<ProcessRuleType> unmet = new ArrayList<>();
        for (ProcessRule rule : rules) {
            if (rule.type().kind() == ProcessRuleKind.GATE && !gateSatisfied.test(rule.type())) {
                canAdvance = false;
                unmet.add(rule.type());
                resetProgress |= rule.policy() == ProcessRulePolicy.RESET_PROGRESS;
            } else if (rule.type().kind() == ProcessRuleKind.MODIFIER) {
                speedMultiplier *= Math.max(0.0D, modifier.apply(rule.type()));
            }
        }
        return new ProcessRuleEvaluation(canAdvance, resetProgress, speedMultiplier, unmet);
    }
}
