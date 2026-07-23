package com.protyvkultury.revivalages.feature.technology.constructionframe;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.constructionframe.block.ConstructionFrameBlock;
import com.protyvkultury.revivalages.feature.technology.constructionframe.blockentity.ConstructionFrameBlockEntity;
import com.protyvkultury.revivalages.feature.technology.constructionframe.client.ConstructionFrameClientEvents;
import com.protyvkultury.revivalages.feature.technology.constructionframe.item.ConstructionFrameItem;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameAssemblyRecipe;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameAssemblyRecipeSerializer;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ConstructionFrameFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, RevivalAges.MOD_ID);

    public static final DeferredBlock<ConstructionFrameBlock> CONSTRUCTION_FRAME = BLOCKS.registerBlock(
            "construction_frame",
            ConstructionFrameBlock::new,
            BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD).noOcclusion()
    );
    public static final DeferredItem<BlockItem> CONSTRUCTION_FRAME_ITEM = ITEMS.register(
            "construction_frame",
            () -> new ConstructionFrameItem(CONSTRUCTION_FRAME.get(), new Item.Properties())
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConstructionFrameBlockEntity>>
            BLOCK_ENTITY = BLOCK_ENTITIES.register(
                    "construction_frame",
                    () -> BlockEntityType.Builder.of(
                            ConstructionFrameBlockEntity::new,
                            CONSTRUCTION_FRAME.get()
                    ).build(null)
            );
    public static final DeferredHolder<RecipeType<?>, RecipeType<FrameAssemblyRecipe>> RECIPE_TYPE =
            RECIPE_TYPES.register("frame_assembly", simpleRecipeType());
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FrameAssemblyRecipe>>
            RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "frame_assembly",
                    FrameAssemblyRecipeSerializer::new
            );
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<FrameEnabledCondition>>
            ENABLED_CONDITION = CONDITIONS.register("construction_frame_enabled", () -> FrameEnabledCondition.CODEC);

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        CONDITIONS.register(modBus);
        modBus.addListener(this::addCreativeItems);
        NeoForge.EVENT_BUS.addListener(this::reloadDisabledRecipeSet);
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                ConstructionFrameConfig.SPEC,
                "revivalages-construction-frame-server.toml"
        );
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ConstructionFrameClientEvents.register(modBus);
        }
    }

    public static boolean visible(Item item) {
        return item != CONSTRUCTION_FRAME_ITEM.get() || ConstructionFrameConfig.enabled();
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (ConstructionFrameConfig.enabled() && event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CONSTRUCTION_FRAME_ITEM.get());
        }
    }

    private void reloadDisabledRecipeSet(ServerStartedEvent event) {
        if (ConstructionFrameConfig.enabled()) {
            return;
        }
        event.getServer()
                .reloadResources(event.getServer().getPackRepository().getSelectedIds())
                .exceptionally(error -> {
                    RevivalAges.LOGGER.error("Unable to activate Construction Frame fallback recipes", error);
                    return null;
                });
    }

    private static Supplier<RecipeType<FrameAssemblyRecipe>> simpleRecipeType() {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id("frame_assembly").toString();
            }
        };
    }
}
