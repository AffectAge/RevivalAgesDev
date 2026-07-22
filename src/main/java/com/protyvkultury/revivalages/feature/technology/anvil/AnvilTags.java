package com.protyvkultury.revivalages.feature.technology.anvil;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class AnvilTags {

    public static final TagKey<Item> HAMMERS = TagKey.create(Registries.ITEM, RevivalAges.id("hammers"));

    private AnvilTags() {
    }
}
