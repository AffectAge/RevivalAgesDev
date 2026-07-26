package com.protyvkultury.revivalages.api.diet;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public final class DietDataMaps {

    public static final DataMapType<Item, DietContribution> ITEM_DIET = DataMapType.builder(
                    RevivalAges.id("item_diet"),
                    Registries.ITEM,
                    DietContribution.CODEC
            )
            .synced(DietContribution.CODEC, true)
            .build();

    private DietDataMaps() {
    }
}
