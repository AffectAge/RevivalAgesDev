package com.protyvkultury.revivalages.api.food;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public final class FoodSpoilageDataMaps {

    public static final DataMapType<Item, FoodSpoilageProfile> ITEM_SPOILAGE = DataMapType.builder(
                    RevivalAges.id("food_spoilage"),
                    Registries.ITEM,
                    FoodSpoilageProfile.CODEC
            )
            .synced(FoodSpoilageProfile.CODEC, true)
            .build();

    private FoodSpoilageDataMaps() {
    }
}
