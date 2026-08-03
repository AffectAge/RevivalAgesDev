package com.protyvkultury.revivalages.core.process;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Loader-neutral viewer description derived from a canonical process rule. */
public record ProcessRuleView(
        ProcessRule rule,
        List<ItemStack> outcomeResults,
        double chance,
        ProcessOutcomeMode outcomeMode,
        int stages) {

    public ProcessRuleView(ProcessRule rule) {
        this(rule, List.of(), 0.0D, null, 0);
    }

    public ProcessRuleView(ProcessRule rule, ItemStack hazardFailure) {
        this(rule, hazardFailure.isEmpty() ? List.of() : List.of(hazardFailure), 0.0D, null, 0);
    }

    public static ProcessRuleView chance(
            double chance, ProcessOutcomeMode mode, int stages, List<ItemStack> outcomeResults) {
        return new ProcessRuleView(
                ProcessRule.of(ProcessRuleType.RANDOM_OUTCOME), outcomeResults, chance, mode, stages);
    }

    public ProcessRuleView {
        outcomeResults = outcomeResults.stream().map(ItemStack::copy).toList();
        if (chance < 0.0D || chance > 1.0D) {
            throw new IllegalArgumentException("Chance must be in [0,1]");
        }
        if (outcomeMode == null && (chance != 0.0D || stages != 0)) {
            throw new IllegalArgumentException("Chance outcomes require a mode");
        }
        if ((outcomeMode == ProcessOutcomeMode.PER_STAGE || outcomeMode == ProcessOutcomeMode.PER_ATTEMPT)
                && stages <= 0) {
            throw new IllegalArgumentException("Staged chance outcomes require positive stages");
        }
    }

    public ProcessRulePresentation presentation() {
        return ProcessRulePresentation.of(rule.type());
    }

    public boolean hasHazardFailure() {
        return rule.type() == ProcessRuleType.WEATHER_EXPOSURE && !outcomeResults.isEmpty();
    }

    public boolean hasChanceOutcome() {
        return rule.type() == ProcessRuleType.RANDOM_OUTCOME;
    }
}
