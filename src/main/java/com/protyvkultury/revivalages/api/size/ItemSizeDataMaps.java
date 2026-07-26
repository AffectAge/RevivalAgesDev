package com.protyvkultury.revivalages.api.size;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * Public data-map keys consumed by item-size providers and container adapters.
 */
public final class ItemSizeDataMaps {

    public static final DataMapType<Item, Size> ITEM_SIZE = DataMapType.builder(
                    RevivalAges.id("item_size"),
                    Registries.ITEM,
                    Size.CODEC
            )
            .synced(Size.CODEC, true)
            .build();
    public static final DataMapType<Item, ContainerSizePolicy> ITEM_CONTAINER = DataMapType.builder(
                    RevivalAges.id("size_container"),
                    Registries.ITEM,
                    ContainerSizePolicy.CODEC
            )
            .synced(ContainerSizePolicy.CODEC, true)
            .build();
    public static final DataMapType<Block, ContainerSizePolicy> BLOCK_CONTAINER = DataMapType.builder(
                    RevivalAges.id("size_container"),
                    Registries.BLOCK,
                    ContainerSizePolicy.CODEC
            )
            .synced(ContainerSizePolicy.CODEC, true)
            .build();

    private ItemSizeDataMaps() {
    }
}
