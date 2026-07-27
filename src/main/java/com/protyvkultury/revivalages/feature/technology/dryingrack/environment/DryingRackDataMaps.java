package com.protyvkultury.revivalages.feature.technology.dryingrack.environment;

import com.mojang.serialization.Codec;
import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public final class DryingRackDataMaps {

    public static final DataMapType<Biome, Double> CRUDE_DRYING_SPEED = DataMapType.builder(
                    RevivalAges.id("crude_drying_speed"),
                    Registries.BIOME,
                    Codec.doubleRange(-100.0D, 100.0D))
            .build();
    public static final DataMapType<Biome, Double> DRYING_SPEED = DataMapType.builder(
                    RevivalAges.id("drying_speed"),
                    Registries.BIOME,
                    Codec.doubleRange(-100.0D, 100.0D))
            .build();

    private DryingRackDataMaps() {
    }
}
