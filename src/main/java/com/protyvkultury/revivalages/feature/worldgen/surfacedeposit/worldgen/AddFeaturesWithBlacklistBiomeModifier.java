package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.worldgen;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.SurfaceDepositFeature;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/** Adds placed features to a union of biome sets minus an explicit blacklist. */
public record AddFeaturesWithBlacklistBiomeModifier(
        List<HolderSet<Biome>> biomes,
        List<HolderSet<Biome>> blacklistBiomes,
        HolderSet<PlacedFeature> features,
        GenerationStep.Decoration step
) implements BiomeModifier {

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD
                || biomes.stream().noneMatch(set -> set.contains(biome))
                || blacklistBiomes.stream().anyMatch(set -> set.contains(biome))) {
            return;
        }
        BiomeGenerationSettingsBuilder generation = builder.getGenerationSettings();
        features.forEach(feature -> generation.addFeature(step, feature));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return SurfaceDepositFeature.ADD_FEATURES_WITH_BLACKLIST.get();
    }
}
