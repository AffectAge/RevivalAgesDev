package com.protyvkultury.revivalages.feature.inventory.carriedweight;

public record WeightFormulaSettings(
        double bucketWeight,
        double bottleWeight,
        double blockWeight,
        double ingotWeight,
        double nuggetWeight,
        double itemWeight,
        double technicalWeight,
        double containerContentsMultiplier,
        double stackMultiplierCoefficient,
        double commonRarityMultiplier,
        double uncommonRarityMultiplier,
        double rareRarityMultiplier,
        double epicRarityMultiplier,
        double fastFoodThresholdSeconds,
        double fireResistantMultiplier,
        double toolDurabilityDivisor,
        double toolDurabilityWeight,
        double armorDurabilityDivisor,
        double armorDurabilityWeight,
        double armorProtectionWeight,
        double blockHardnessWeight,
        double blockResistanceWeight,
        double blockResistanceWeightCap,
        double transparentBlockMultiplier,
        double blockEntityWeight,
        double slabMultiplier,
        double stairsMultiplier
) {
}
