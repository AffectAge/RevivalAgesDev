package com.protyvkultury.revivalages.api.weight;

import com.mojang.serialization.Codec;
import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * Public synchronized data-map keys for fixed per-unit weights and pockets.
 */
public final class ItemWeightDataMaps {

    public static final DataMapType<Item, Double> ITEM_WEIGHT = DataMapType.builder(
                    RevivalAges.id("item_weight"),
                    Registries.ITEM,
                    Codec.doubleRange(0.0D, Double.MAX_VALUE)
            )
            .synced(Codec.doubleRange(0.0D, Double.MAX_VALUE), true)
            .build();
    public static final DataMapType<Item, Integer> POCKETS = DataMapType.builder(
                    RevivalAges.id("pockets"),
                    Registries.ITEM,
                    Codec.intRange(0, 1_000_000)
            )
            .synced(Codec.intRange(0, 1_000_000), true)
            .build();

    private ItemWeightDataMaps() {
    }
}
