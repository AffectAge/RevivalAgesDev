package com.protyvkultury.revivalages.data.itemsize;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.size.ContainerSizePolicy;
import com.protyvkultury.revivalages.api.size.ItemSizeDataMaps;
import com.protyvkultury.revivalages.api.size.Size;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class ItemSizeDataProvider extends DataMapProvider {

    private static final Map<ResourceLocation, Size> REVIVAL_SIZES = revivalSizes();

    public ItemSizeDataProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        DataMapProvider.Builder<Size, Item> sizes = builder(ItemSizeDataMaps.ITEM_SIZE);
        addVanillaAndCommonSizes(sizes);
        REVIVAL_SIZES.forEach((id, size) -> sizes.add(id, size, true));
        validateRevivalItems();

        builder(ItemSizeDataMaps.ITEM_CONTAINER)
                .add(BuiltInRegistries.ITEM.getKey(Items.BUNDLE), new ContainerSizePolicy(Size.NORMAL), false);
        builder(ItemSizeDataMaps.BLOCK_CONTAINER)
                .add(BuiltInRegistries.BLOCK.getKey(Blocks.CHEST), new ContainerSizePolicy(Size.LARGE), false)
                .add(
                        BuiltInRegistries.BLOCK.getKey(Blocks.TRAPPED_CHEST),
                        new ContainerSizePolicy(Size.LARGE),
                        false
                );
    }

    @Override
    public String getName() {
        return "Revival Ages Item Size Data Maps";
    }

    private static void addVanillaAndCommonSizes(DataMapProvider.Builder<Size, Item> sizes) {
        addTags(sizes, Size.TINY, "c:dyes");
        addTags(sizes, Size.VERY_SMALL, "c:dusts", "minecraft:signs", "minecraft:hanging_signs");
        addTags(sizes, Size.SMALL, "c:foods", "minecraft:slabs", "minecraft:stairs");
        sizes.add(BuiltInRegistries.ITEM.getKey(Items.BOWL), Size.SMALL, true);
        addTags(sizes, Size.NORMAL, "c:rods");
        sizes.add(BuiltInRegistries.ITEM.getKey(Items.GLASS_BOTTLE), Size.NORMAL, true);
        addTags(
                sizes,
                Size.LARGE,
                "c:buckets",
                "c:chests",
                "c:ingots",
                "minecraft:rails",
                "minecraft:trapdoors"
        );
        addTags(
                sizes,
                Size.VERY_LARGE,
                "minecraft:logs",
                "minecraft:doors",
                "minecraft:boats",
                "c:minecarts",
                "minecraft:pickaxes",
                "minecraft:shovels",
                "minecraft:hoes",
                "c:tools/hammer",
                "c:tools/saw",
                "c:tools/fishing_rod",
                "c:tools/shield",
                "c:tools/melee_weapon",
                "c:tools/ranged_weapon"
        );
        sizes.add(BuiltInRegistries.ITEM.getKey(Items.ANVIL), Size.HUGE, true);
        sizes.add(BuiltInRegistries.ITEM.getKey(Items.CHIPPED_ANVIL), Size.HUGE, true);
        sizes.add(BuiltInRegistries.ITEM.getKey(Items.DAMAGED_ANVIL), Size.HUGE, true);
    }

    @SafeVarargs
    private static void addTags(DataMapProvider.Builder<Size, Item> sizes, Size size, String... ids) {
        for (String value : ids) {
            ResourceLocation id = ResourceLocation.parse(value);
            sizes.add(TagKey.create(Registries.ITEM, id), size, true);
        }
    }

    private static void validateRevivalItems() {
        List<ResourceLocation> missing = ContentAvailability.itemMemberships().keySet().stream()
                .filter(id -> id.getNamespace().equals(RevivalAges.MOD_ID))
                .filter(id -> !REVIVAL_SIZES.containsKey(id))
                .sorted()
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Public Revival Ages items without an explicit Item Size: " + missing);
        }
    }

    private static Map<ResourceLocation, Size> revivalSizes() {
        Map<ResourceLocation, Size> values = new LinkedHashMap<>();
        add(values, Size.VERY_SMALL,
                "rock", "granite_rock", "diorite_rock", "andesite_rock", "sand_rock", "red_sand_rock",
                "gravel_rock", "end_stone_rock", "netherrack_rock", "soul_soil_rock",
                "cobblestone_splitter", "granite_splitter", "diorite_splitter", "andesite_splitter",
                "sandstone_splitter", "red_sandstone_splitter", "end_stone_splitter",
                "netherrack_splitter", "soul_soil_splitter",
                "oak_stick", "spruce_stick", "birch_stick", "acacia_stick", "jungle_stick",
                "dark_oak_stick", "mangrove_stick", "cherry_stick", "bamboo_stick",
                "crimson_stick", "warped_stick",
                "wood_chips", "pit_ash", "tinder", "wood_torch"
        );
        add(values, Size.SMALL,
                "straw", "raw_hide", "scraped_hide", "washed_hide", "tanned_hide", "burned_food",
                "unfired_brick", "thatch", "construction_frame"
        );
        add(values, Size.NORMAL, "stone_saw_blade", "flint_saw_blade", "bone_saw_blade");
        add(values, Size.LARGE,
                "wooden_bucket", "unfired_clay_bucket", "clay_bucket", "tannin_bucket",
                "flint_and_tinder", "barrel_lid",
                "oak_support_beam", "spruce_support_beam", "birch_support_beam",
                "jungle_support_beam", "acacia_support_beam", "dark_oak_support_beam",
                "mangrove_support_beam", "cherry_support_beam", "bamboo_support_beam",
                "crude_drying_rack", "drying_rack", "tanning_rack", "pit_kiln"
        );
        add(values, Size.VERY_LARGE, "stone_hammer", "chopping_block", "log_pile", "hand_grindstone");
        add(values, Size.HUGE,
                "barrel", "storage_barrel", "soaking_pot", "anvil",
                "stone_sawmill", "stone_oven", "stone_kiln", "stone_crucible",
                "horse_grindstone", "horse_chopping_block", "horse_press"
        );
        return Map.copyOf(values);
    }

    private static void add(Map<ResourceLocation, Size> values, Size size, String... paths) {
        for (String path : paths) {
            ResourceLocation id = RevivalAges.id(path);
            Size previous = values.put(id, size);
            if (previous != null) {
                throw new IllegalStateException("Duplicate Item Size assignment for " + id);
            }
        }
    }
}
