package com.protyvkultury.revivalages.feature.inventory.carriedweight;

final class WeightPenaltyMath {

    private WeightPenaltyMath() {
    }

    static int overloadLevel(
            double current,
            double capacity,
            int strengthAmplifier,
            int hasteAmplifier,
            WeightPenaltySettings settings
    ) {
        double percentage = capacity <= 0.0D ? 100.0D : current / capacity * 100.0D;
        int rawLevel = 1 + Math.max(
                0,
                (int) Math.floor((percentage - 100.0D) / settings.overloadStepPercent())
        );
        int adjusted = Math.max(1, rawLevel - strengthAmplifier - hasteAmplifier);
        return Math.min(adjusted, settings.maximumOverloadLevel());
    }

    static Penalties overloadPenalties(int level, WeightPenaltySettings settings) {
        double additional = settings.overloadLevelPenalty() * (level - 1) * settings.strength();
        return new Penalties(
                cap(settings.overloadBasePenalty() + additional, settings),
                cap(settings.overloadBasePenalty() + additional, settings),
                cap(settings.overloadDamageBasePenalty() + additional, settings)
        );
    }

    static Penalties realisticPenalties(
            double current,
            double capacity,
            WeightPenaltySettings settings
    ) {
        double factor = realisticFactor(current, capacity, settings);
        double full = Math.min(factor * settings.maximumAttributePenalty(), settings.maximumAttributePenalty());
        return new Penalties(
                cap(full * (1.0D - settings.realisticSpeedRelief()), settings),
                cap(full * (1.0D - settings.realisticAttackSpeedRelief()), settings),
                cap(full * (1.0D - settings.realisticDamageRelief()), settings)
        );
    }

    static double jumpVelocity(
            double base,
            double current,
            double capacity,
            int overloadLevel,
            WeightPenaltySettings settings
    ) {
        if (settings.strength() <= 0.0D || capacity <= 0.0D) {
            return base;
        }
        if (current >= capacity) {
            double reduced = base * settings.overloadJumpMultiplier()
                    / Math.max(1, overloadLevel)
                    / settings.strength();
            return Math.max(reduced, base * settings.overloadMinimumJumpMultiplier());
        }
        if (settings.realisticMode()
                && current > capacity * settings.realisticStartFraction()) {
            double factor = realisticFactor(current, capacity, settings);
            double reduced = base * (1.0D - factor * settings.overloadBasePenalty());
            return Math.max(reduced, base * settings.realisticMinimumJumpMultiplier())
                    + settings.realisticJumpBonus();
        }
        return base;
    }

    private static double realisticFactor(
            double current,
            double capacity,
            WeightPenaltySettings settings
    ) {
        double start = settings.realisticStartFraction() * capacity;
        double range = Math.max(1.0D, capacity - start);
        return Math.max(0.0D, Math.min(1.0D, (current - start) / range));
    }

    private static double cap(double value, WeightPenaltySettings settings) {
        return Math.max(0.0D, Math.min(value, settings.maximumAttributePenalty()));
    }

    record Penalties(double movementSpeed, double attackSpeed, double attackDamage) {
    }
}
