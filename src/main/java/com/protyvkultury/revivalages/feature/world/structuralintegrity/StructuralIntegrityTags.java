package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class StructuralIntegrityTags {

    public static final TagKey<Block> SUPPORT_BEAMS = block("support_beams");
    public static final TagKey<Block> CAN_TRIGGER_COLLAPSE = block("can_trigger_collapse");
    public static final TagKey<Block> CAN_START_COLLAPSE = block("can_start_collapse");
    public static final TagKey<Block> CAN_COLLAPSE = block("can_collapse");
    public static final TagKey<Block> CAN_LANDSLIDE = block("can_landslide");
    public static final TagKey<Block> COLLAPSES_TO_COBBLED_DEEPSLATE =
            block("collapses_to_cobbled_deepslate");
    public static final TagKey<Block> NOT_SOLID_SUPPORTING = block("not_solid_supporting");
    public static final TagKey<Block> SUPPORTS_LANDSLIDE = block("supports_landslide");
    public static final TagKey<Block> TOUGHNESS_1 = block("toughness_1");
    public static final TagKey<Block> TOUGHNESS_2 = block("toughness_2");
    public static final TagKey<Block> TOUGHNESS_3 = block("toughness_3");

    public static final TagKey<Item> SUPPORT_BEAM_ITEMS = item("support_beams");
    public static final TagKey<Item> SAW_BLADES = item("saw_blades");

    private StructuralIntegrityTags() {
    }

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, RevivalAges.id(path));
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, RevivalAges.id(path));
    }
}
