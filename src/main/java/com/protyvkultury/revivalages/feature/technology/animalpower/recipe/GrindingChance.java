package com.protyvkultury.revivalages.feature.technology.animalpower.recipe;

/** Keeps the secondary-result roll deterministic and independently testable. */
public final class GrindingChance {

    private GrindingChance() {
    }

    public static boolean shouldProduce(double chance, double roll) {
        if (chance < 0.0D || chance > 1.0D) {
            throw new IllegalArgumentException("chance must be between zero and one");
        }
        if (roll < 0.0D || roll >= 1.0D) {
            throw new IllegalArgumentException("roll must be in the range [0, 1)");
        }
        return roll < chance;
    }
}
