package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

final class CarriedWeightTags {

    static final TagKey<Item> TECHNICAL_ITEMS = own("technical_weight_items");
    static final TagKey<Item> BUCKETS = common("buckets");
    static final TagKey<Item> BOTTLES = common("bottles");
    static final TagKey<Item> INGOTS = common("ingots");
    static final TagKey<Item> GEMS = common("gems");
    static final TagKey<Item> SHARDS = common("shards");
    static final TagKey<Item> NUGGETS = common("nuggets");

    private CarriedWeightTags() {
    }

    private static TagKey<Item> own(String path) {
        return TagKey.create(Registries.ITEM, RevivalAges.id(path));
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", path)
        );
    }
}
