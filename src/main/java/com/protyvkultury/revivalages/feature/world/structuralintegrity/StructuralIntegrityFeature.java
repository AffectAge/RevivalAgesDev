package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.client.StructuralIntegrityClientEvents;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.network.CollapseShakePayload;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.block.HorizontalSupportBlock;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.block.VerticalSupportBlock;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe.BlockTransformationRecipe;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe.BlockTransformationRecipeSerializer;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe.SupportBeamRecipe;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe.SupportBeamRecipeSerializer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class StructuralIntegrityFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, RevivalAges.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, RevivalAges.MOD_ID);

    public static final DataMapType<net.minecraft.world.level.block.Block, SupportDefinition> SUPPORT_DATA =
            DataMapType.builder(
                    RevivalAges.id("support"),
                    Registries.BLOCK,
                    SupportDefinition.CODEC
            ).build();

    public static final DeferredHolder<RecipeType<?>, RecipeType<BlockTransformationRecipe>> COLLAPSE_TYPE =
            RECIPE_TYPES.register("collapse", recipeType("collapse"));
    public static final DeferredHolder<RecipeType<?>, RecipeType<BlockTransformationRecipe>> LANDSLIDE_TYPE =
            RECIPE_TYPES.register("landslide", recipeType("landslide"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BlockTransformationRecipe>>
            COLLAPSE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "collapse",
                    () -> new BlockTransformationRecipeSerializer(BlockTransformationRecipe.Kind.COLLAPSE)
            );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BlockTransformationRecipe>>
            LANDSLIDE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "landslide",
                    () -> new BlockTransformationRecipeSerializer(BlockTransformationRecipe.Kind.LANDSLIDE)
            );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SupportBeamRecipe>>
            SUPPORT_BEAM_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "support_beam",
                    SupportBeamRecipeSerializer::new
            );
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<StructuralEnabledCondition>>
            ENABLED_CONDITION = CONDITIONS.register(
                    "structural_enabled",
                    () -> StructuralEnabledCondition.CODEC
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> COLLAPSE_WARNING = sound("collapse_warning");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLAPSE_START = sound("collapse_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLLAPSE_IMPACT = sound("collapse_impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> LANDSLIDE_IMPACT = sound("landslide_impact");
    public static final DeferredHolder<EntityType<?>, EntityType<StructuralFallingBlockEntity>>
            FALLING_BLOCK_ENTITY = ENTITY_TYPES.register(
                    "structural_falling_block",
                    () -> EntityType.Builder.<StructuralFallingBlockEntity>of(
                                    StructuralFallingBlockEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.98F, 0.98F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build("structural_falling_block")
            );

    private static final Map<SupportWood, DeferredBlock<VerticalSupportBlock>> VERTICAL_SUPPORTS =
            new EnumMap<>(SupportWood.class);
    private static final Map<SupportWood, DeferredBlock<HorizontalSupportBlock>> HORIZONTAL_SUPPORTS =
            new EnumMap<>(SupportWood.class);
    private static final Map<SupportWood, DeferredItem<StandingAndWallBlockItem>> SUPPORT_ITEMS =
            new EnumMap<>(SupportWood.class);

    static {
        for (SupportWood wood : SupportWood.values()) {
            String baseName = wood.serializedName() + "_support_beam";
            DeferredBlock<VerticalSupportBlock> vertical = BLOCKS.registerBlock(
                    baseName,
                    VerticalSupportBlock::new,
                    supportProperties()
            );
            DeferredBlock<HorizontalSupportBlock> horizontal = BLOCKS.registerBlock(
                    baseName + "_horizontal",
                    HorizontalSupportBlock::new,
                    supportProperties()
            );
            DeferredItem<StandingAndWallBlockItem> item = ITEMS.register(
                    baseName,
                    () -> new StandingAndWallBlockItem(
                            vertical.get(),
                            horizontal.get(),
                            new Item.Properties(),
                            Direction.DOWN
                    )
            );
            VERTICAL_SUPPORTS.put(wood, vertical);
            HORIZONTAL_SUPPORTS.put(wood, horizontal);
            SUPPORT_ITEMS.put(wood, item);
        }
    }

    @Override
    public ContentPolicy contentPolicy() {
        ContentPolicy.Builder policy = ContentPolicy.gameplay("structural_integrity")
                .define(
                        ContentKey.STRUCTURAL_INTEGRITY,
                        () -> StructuralIntegrityConfig.configuredEnabled(ContentKey.STRUCTURAL_INTEGRITY)
                )
                .define(
                        ContentKey.SUPPORT_BEAMS,
                        () -> StructuralIntegrityConfig.configuredEnabled(ContentKey.SUPPORT_BEAMS)
                )
                .define(
                        ContentKey.COLLAPSES,
                        () -> StructuralIntegrityConfig.configuredEnabled(ContentKey.COLLAPSES)
                )
                .define(
                        ContentKey.LANDSLIDES,
                        () -> StructuralIntegrityConfig.configuredEnabled(ContentKey.LANDSLIDES)
                );
        for (SupportWood wood : SupportWood.values()) {
            policy.items(ContentKey.SUPPORT_BEAMS, wood.serializedName() + "_support_beam");
            policy.blocks(ContentKey.SUPPORT_BEAMS, wood.serializedName() + "_support_beam_horizontal");
        }
        return policy.build();
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        SOUNDS.register(modBus);
        ENTITY_TYPES.register(modBus);
        CONDITIONS.register(modBus);
        modBus.addListener(this::registerDataMaps);
        modBus.addListener(this::registerPayloads);
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                StructuralIntegrityConfig.SPEC,
                "revivalages-structural-integrity-server.toml"
        );
        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                StructuralIntegrityConfig.CLIENT_SPEC,
                "revivalages-structural-integrity-client.toml"
        );
        NeoForge.EVENT_BUS.addListener(StructuralSimulation::onBlockBroken);
        NeoForge.EVENT_BUS.addListener(StructuralSimulation::onNeighborUpdate);
        NeoForge.EVENT_BUS.addListener(StructuralSimulation::onExplosion);
        NeoForge.EVENT_BUS.addListener(StructuralSimulation::onLevelTick);
        NeoForge.EVENT_BUS.addListener(SupportService::refreshRanges);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            StructuralIntegrityClientEvents.register(modBus);
        }
    }

    public static Map<SupportWood, DeferredItem<StandingAndWallBlockItem>> supportItems() {
        return Collections.unmodifiableMap(SUPPORT_ITEMS);
    }

    public static VerticalSupportBlock verticalSupport(SupportWood wood) {
        return VERTICAL_SUPPORTS.get(wood).get();
    }

    public static HorizontalSupportBlock horizontalSupport(SupportWood wood) {
        return HORIZONTAL_SUPPORTS.get(wood).get();
    }

    public static boolean visible(Item item) {
        return StructuralIntegrityConfig.supportBeamsEnabled()
                || SUPPORT_ITEMS.values().stream().noneMatch(holder -> holder.get() == item);
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(SUPPORT_DATA);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                CollapseShakePayload.TYPE,
                CollapseShakePayload.STREAM_CODEC,
                CollapseShakePayload::handle
        );
    }

    private static BlockBehaviour.Properties supportProperties() {
        return BlockBehaviour.Properties.of()
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(RevivalAges.id(name)));
    }

    private static Supplier<RecipeType<BlockTransformationRecipe>> recipeType(String name) {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id(name).toString();
            }
        };
    }
}
