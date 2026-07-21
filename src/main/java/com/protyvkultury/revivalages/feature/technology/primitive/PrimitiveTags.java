package com.protyvkultury.revivalages.feature.technology.primitive;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityType;

public final class PrimitiveTags {

    public static final TagKey<Item> CAMPFIRE_FUELS = item("campfire_fuels");
    public static final TagKey<Item> PIT_KILN_LOGS = item("pit_kiln_logs");
    public static final TagKey<Item> BARREL_LEAVES = item("barrel_leaves");
    public static final TagKey<Item> CHOPPING_AXES = item("chopping_axes");
    public static final TagKey<Item> INVALID_CHOPPING_AXES = item("invalid_chopping_axes");
    public static final TagKey<Block> CAMPFIRE_OCCUPANTS = block("campfire_occupants");
    public static final TagKey<Block> PIT_KILN_STRUCTURE_BLOCKS = block("pit_kiln_structure_blocks");
    public static final TagKey<Block> PIT_KILN_REFRACTORY_BLOCKS = block("pit_kiln_refractory_blocks");
    public static final TagKey<EntityType<?>> DROPS_RAW_HIDE = entityType("drops_raw_hide");

    private PrimitiveTags() {
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, RevivalAges.id(path));
    }

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, RevivalAges.id(path));
    }

    private static TagKey<EntityType<?>> entityType(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, RevivalAges.id(path));
    }
}
