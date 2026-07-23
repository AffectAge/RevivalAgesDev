package com.protyvkultury.revivalages.feature.technology.animalpower;

/** Defines the default chopping-cycle and output progression shared by runtime logic and tests. */
public final class AnimalChoppingProfile {

    private AnimalChoppingProfile() {
    }

    public static int cyclesForTier(int tier) {
        return switch (tier) {
            case 0 -> 6;
            case 1 -> 5;
            case 2 -> 4;
            default -> 3;
        };
    }

    public static int quantityForTier(int tier) {
        return switch (tier) {
            case 0 -> 2;
            case 1 -> 3;
            case 2 -> 4;
            default -> 5;
        };
    }
}
