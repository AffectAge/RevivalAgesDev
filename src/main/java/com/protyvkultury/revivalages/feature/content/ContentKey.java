package com.protyvkultury.revivalages.feature.content;

import com.protyvkultury.revivalages.RevivalAges;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Stable identifiers for server-controlled gameplay availability.
 *
 * <p>Registry entries never depend on these keys. A key only controls normal
 * acquisition, presentation, and behavior.</p>
 */
public enum ContentKey {
    ITEM_SIZE("item_size"),

    SURFACE_DEPOSITS("surface_deposits"),
    SURFACE_ROCKS("surface_rocks", SURFACE_DEPOSITS),
    SURFACE_STICKS("surface_sticks", SURFACE_DEPOSITS),

    KNAPPING("knapping"),
    CONSTRUCTION_FRAME("construction_frame"),

    STRUCTURAL_INTEGRITY("structural_integrity"),
    SUPPORT_BEAMS("support_beams", STRUCTURAL_INTEGRITY),
    COLLAPSES("collapses", STRUCTURAL_INTEGRITY),
    LANDSLIDES("landslides", STRUCTURAL_INTEGRITY),

    PRIMITIVE_TECHNOLOGY("primitive_technology"),
    RAW_HIDE_DROPS("raw_hide_drops", PRIMITIVE_TECHNOLOGY),
    CRUDE_DRYING_RACK("crude_drying_rack", PRIMITIVE_TECHNOLOGY),
    DRYING_RACK("drying_rack", PRIMITIVE_TECHNOLOGY),
    CAMPFIRE("campfire", PRIMITIVE_TECHNOLOGY),
    CAMPFIRE_EFFECTS("campfire_effects", CAMPFIRE),
    CHOPPING_BLOCK("chopping_block", PRIMITIVE_TECHNOLOGY),
    PIT_KILN("pit_kiln", PRIMITIVE_TECHNOLOGY),
    BARREL("barrel", PRIMITIVE_TECHNOLOGY),
    SOAKING_POT("soaking_pot", PRIMITIVE_TECHNOLOGY),
    TANNING_RACK("tanning_rack", PRIMITIVE_TECHNOLOGY),
    STONE_SAWMILL("stone_sawmill", PRIMITIVE_TECHNOLOGY),
    STONE_OVEN("stone_oven", PRIMITIVE_TECHNOLOGY),
    STONE_KILN("stone_kiln", PRIMITIVE_TECHNOLOGY),
    STONE_CRUCIBLE("stone_crucible", PRIMITIVE_TECHNOLOGY),
    ANVIL("anvil", PRIMITIVE_TECHNOLOGY),
    PIT_BURN("pit_burn", PRIMITIVE_TECHNOLOGY),
    FLINT_AND_TINDER("flint_and_tinder", PRIMITIVE_TECHNOLOGY),
    WOOD_TORCH("wood_torch", PRIMITIVE_TECHNOLOGY),
    WOODEN_BUCKET("wooden_bucket", PRIMITIVE_TECHNOLOGY),
    CLAY_BUCKET("clay_bucket", PRIMITIVE_TECHNOLOGY),

    ANIMAL_POWER("animal_power"),
    HAND_GRINDSTONE("hand_grindstone", ANIMAL_POWER),
    HORSE_GRINDSTONE("horse_grindstone", ANIMAL_POWER),
    HORSE_CHOPPING_BLOCK("horse_chopping_block", ANIMAL_POWER),
    HORSE_PRESS("horse_press", ANIMAL_POWER);

    private final String path;
    private final List<ContentKey> parents;

    ContentKey(String path, ContentKey... parents) {
        this.path = path;
        this.parents = List.of(parents);
    }

    public String path() {
        return path;
    }

    public ResourceLocation id() {
        return RevivalAges.id(path);
    }

    public List<ContentKey> parents() {
        return parents;
    }

    public Component displayName() {
        return Component.translatable("content.revivalages." + path);
    }

    public static ContentKey fromId(ResourceLocation id) {
        if (!id.getNamespace().equals(RevivalAges.MOD_ID)) {
            return null;
        }
        for (ContentKey key : values()) {
            if (key.path.equals(id.getPath())) {
                return key;
            }
        }
        return null;
    }
}
