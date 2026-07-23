package com.protyvkultury.revivalages.feature.creative;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.ignition.IgnitionFeature;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One authoritative tab which automatically includes every public registered mod item. */
public final class CreativeTabFeature implements FeatureModule {

    private static final List<String> PROGRESSION_ORDER = List.of(
            // Gathered natural materials and early components.
            "rock", "granite_rock", "diorite_rock", "andesite_rock", "sand_rock", "red_sand_rock",
            "gravel_rock", "end_stone_rock", "netherrack_rock", "soul_soil_rock",
            "oak_stick", "spruce_stick", "birch_stick", "acacia_stick", "jungle_stick", "dark_oak_stick",
            "mangrove_stick", "cherry_stick", "bamboo_stick", "crimson_stick", "warped_stick",
            "cobblestone_splitter", "granite_splitter", "diorite_splitter", "andesite_splitter",
            "sandstone_splitter", "red_sandstone_splitter", "end_stone_splitter", "netherrack_splitter",
            "soul_soil_splitter", "straw", "wood_chips", "pit_ash", "unfired_brick", "burned_food",
            "raw_hide", "scraped_hide", "washed_hide", "tanned_hide", "thatch",
            // Hand tools, ignition, and portable vessels.
            "tinder", "flint_and_tinder", "stone_hammer", "stone_saw_blade", "flint_saw_blade",
            "bone_saw_blade", "wood_torch", "wooden_bucket", "unfired_clay_bucket", "clay_bucket",
            "tannin_bucket", "barrel_lid",
            // Primitive workstations.
            "hand_grindstone", "crude_drying_rack", "drying_rack", "chopping_block", "pit_kiln", "log_pile",
            "barrel", "soaking_pot", "tanning_rack",
            // Stone-age workstations.
            "horse_grindstone", "horse_chopping_block", "horse_press",
            "stone_sawmill", "stone_oven", "stone_kiln", "stone_crucible", "anvil"
    );
    private static final Map<String, Integer> PROGRESSION_INDEX = createProgressionIndex();

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RevivalAges.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "revival_ages",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.revivalages"))
                    .icon(() -> new ItemStack(IgnitionFeature.WOOD_TORCH_ITEM.get()))
                    .displayItems((parameters, output) -> BuiltInRegistries.ITEM.entrySet().stream()
                            .filter(entry -> entry.getKey().location().getNamespace().equals(RevivalAges.MOD_ID))
                            .sorted(Comparator
                                    .comparingInt(CreativeTabFeature::progressionIndex)
                                    .thenComparing(entry -> entry.getKey().location().toString()))
                            .forEach(entry -> output.accept(entry.getValue())))
                    .build());

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        TABS.register(modBus);
    }

    private static Map<String, Integer> createProgressionIndex() {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < PROGRESSION_ORDER.size(); i++) {
            result.put(PROGRESSION_ORDER.get(i), i);
        }
        return Map.copyOf(result);
    }

    private static int progressionIndex(Map.Entry<ResourceKey<Item>, Item> entry) {
        return PROGRESSION_INDEX.getOrDefault(entry.getKey().location().getPath(), Integer.MAX_VALUE);
    }
}
