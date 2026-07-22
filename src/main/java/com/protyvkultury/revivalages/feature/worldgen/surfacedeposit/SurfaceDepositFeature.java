package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.block.RockDepositBlock;
import com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.block.StickDepositBlock;
import com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.worldgen.AddFeaturesWithBlacklistBiomeModifier;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class SurfaceDepositFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, RevivalAges.MOD_ID);

    public static final DeferredBlock<RockDepositBlock> ROCK = rock("rock");
    public static final DeferredBlock<RockDepositBlock> GRANITE_ROCK = rock("granite_rock");
    public static final DeferredBlock<RockDepositBlock> DIORITE_ROCK = rock("diorite_rock");
    public static final DeferredBlock<RockDepositBlock> ANDESITE_ROCK = rock("andesite_rock");
    public static final DeferredBlock<RockDepositBlock> SAND_ROCK = rock("sand_rock");
    public static final DeferredBlock<RockDepositBlock> RED_SAND_ROCK = rock("red_sand_rock");
    public static final DeferredBlock<RockDepositBlock> GRAVEL_ROCK = rock("gravel_rock");
    public static final DeferredBlock<RockDepositBlock> END_STONE_ROCK = rock("end_stone_rock");
    public static final DeferredBlock<RockDepositBlock> NETHERRACK_ROCK = rock("netherrack_rock");
    public static final DeferredBlock<RockDepositBlock> SOUL_SOIL_ROCK = rock("soul_soil_rock");

    public static final DeferredBlock<StickDepositBlock> OAK_STICK = stick("oak_stick");
    public static final DeferredBlock<StickDepositBlock> SPRUCE_STICK = stick("spruce_stick");
    public static final DeferredBlock<StickDepositBlock> BIRCH_STICK = stick("birch_stick");
    public static final DeferredBlock<StickDepositBlock> ACACIA_STICK = stick("acacia_stick");
    public static final DeferredBlock<StickDepositBlock> JUNGLE_STICK = stick("jungle_stick");
    public static final DeferredBlock<StickDepositBlock> DARK_OAK_STICK = stick("dark_oak_stick");
    public static final DeferredBlock<StickDepositBlock> MANGROVE_STICK = stick("mangrove_stick");
    public static final DeferredBlock<StickDepositBlock> CHERRY_STICK = stick("cherry_stick");
    public static final DeferredBlock<StickDepositBlock> BAMBOO_STICK = stick("bamboo_stick");
    public static final DeferredBlock<StickDepositBlock> CRIMSON_STICK = stick("crimson_stick");
    public static final DeferredBlock<StickDepositBlock> WARPED_STICK = stick("warped_stick");

    public static final DeferredItem<Item> COBBLESTONE_SPLITTER = splitter("cobblestone_splitter");
    public static final DeferredItem<Item> GRANITE_SPLITTER = splitter("granite_splitter");
    public static final DeferredItem<Item> DIORITE_SPLITTER = splitter("diorite_splitter");
    public static final DeferredItem<Item> ANDESITE_SPLITTER = splitter("andesite_splitter");
    public static final DeferredItem<Item> SANDSTONE_SPLITTER = splitter("sandstone_splitter");
    public static final DeferredItem<Item> RED_SANDSTONE_SPLITTER = splitter("red_sandstone_splitter");
    public static final DeferredItem<Item> END_STONE_SPLITTER = splitter("end_stone_splitter");
    public static final DeferredItem<Item> NETHERRACK_SPLITTER = splitter("netherrack_splitter");
    public static final DeferredItem<Item> SOUL_SOIL_SPLITTER = splitter("soul_soil_splitter");

    public static final net.neoforged.neoforge.registries.DeferredHolder<
            MapCodec<? extends BiomeModifier>, MapCodec<AddFeaturesWithBlacklistBiomeModifier>>
            ADD_FEATURES_WITH_BLACKLIST = BIOME_MODIFIER_SERIALIZERS.register(
                    "add_features_with_blacklist",
                    () -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Biome.LIST_CODEC.listOf().fieldOf("whitelist")
                                    .forGetter(AddFeaturesWithBlacklistBiomeModifier::biomes),
                            Biome.LIST_CODEC.listOf().optionalFieldOf("blacklist", List.of())
                                    .forGetter(AddFeaturesWithBlacklistBiomeModifier::blacklistBiomes),
                            PlacedFeature.LIST_CODEC.fieldOf("features")
                                    .forGetter(AddFeaturesWithBlacklistBiomeModifier::features),
                            GenerationStep.Decoration.CODEC.fieldOf("step")
                                    .forGetter(AddFeaturesWithBlacklistBiomeModifier::step)
                    ).apply(instance, AddFeaturesWithBlacklistBiomeModifier::new))
            );

    private static DeferredBlock<RockDepositBlock> rock(String id) {
        DeferredBlock<RockDepositBlock> block = BLOCKS.registerBlock(
                id,
                RockDepositBlock::new,
                BlockBehaviour.Properties.of()
                        .noCollission()
                        .noOcclusion()
                        .instabreak()
                        .sound(SoundType.STONE)
        );
        ITEMS.registerSimpleBlockItem(block, new Item.Properties());
        return block;
    }

    private static DeferredBlock<StickDepositBlock> stick(String id) {
        DeferredBlock<StickDepositBlock> block = BLOCKS.registerBlock(
                id,
                StickDepositBlock::new,
                BlockBehaviour.Properties.of()
                        .noCollission()
                        .noOcclusion()
                        .instabreak()
                        .sound(SoundType.WOOD)
        );
        ITEMS.registerSimpleBlockItem(block, new Item.Properties());
        return block;
    }

    private static DeferredItem<Item> splitter(String id) {
        return ITEMS.registerSimpleItem(id, new Item.Properties());
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
    }
}
