package com.protyvkultury.revivalages.feature.technology.dryingrack;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.dryingrack.block.CrudeDryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.dryingrack.block.DryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.dryingrack.client.DryingRackClientEvents;
import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackConfig;
import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackClientConfig;
import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipe;
import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipeSerializer;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DryingRackFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);

    public static final DeferredBlock<CrudeDryingRackBlock> CRUDE_DRYING_RACK = BLOCKS.registerBlock(
            "crude_drying_rack",
            CrudeDryingRackBlock::new,
            rackProperties()
    );
    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = BLOCKS.registerBlock(
            "drying_rack",
            DryingRackBlock::new,
            rackProperties()
    );
    public static final DeferredItem<BlockItem> CRUDE_DRYING_RACK_ITEM =
            ITEMS.registerSimpleBlockItem(CRUDE_DRYING_RACK, new Item.Properties());
    public static final DeferredItem<BlockItem> DRYING_RACK_ITEM =
            ITEMS.registerSimpleBlockItem(DRYING_RACK, new Item.Properties());

    public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRecipe>> CRUDE_DRYING_RECIPE_TYPE =
            RECIPE_TYPES.register("crude_drying", simpleRecipeType("crude_drying"));
    public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRecipe>> DRYING_RECIPE_TYPE =
            RECIPE_TYPES.register("drying", simpleRecipeType("drying"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DryingRecipe>>
            CRUDE_DRYING_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "crude_drying",
                    () -> new DryingRecipeSerializer(CRUDE_DRYING_RECIPE_TYPE)
            );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DryingRecipe>> DRYING_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register(
                    "drying",
                    () -> new DryingRecipeSerializer(DRYING_RECIPE_TYPE)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>>
            CRUDE_DRYING_RACK_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "crude_drying_rack",
                    () -> BlockEntityType.Builder.of(
                            DryingRackBlockEntity::createCrude,
                            CRUDE_DRYING_RACK.get()
                    ).build(null)
            );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>>
            DRYING_RACK_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "drying_rack",
                    () -> BlockEntityType.Builder.of(
                            DryingRackBlockEntity::createNormal,
                            DRYING_RACK.get()
                    ).build(null)
            );

    private static BlockBehaviour.Properties rackProperties() {
        return BlockBehaviour.Properties.of()
                .strength(0.5F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .ignitedByLava();
    }

    private static Supplier<RecipeType<DryingRecipe>> simpleRecipeType(String name) {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id(name).toString();
            }
        };
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(this::addCreativeTabItems);
        modBus.addListener(this::registerCapabilities);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            DryingRackClientEvents.register(modBus);
        }
        modContainer.registerConfig(ModConfig.Type.SERVER, DryingRackConfig.SPEC, "revivalages-server.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, DryingRackClientConfig.SPEC, "revivalages-client.toml");
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CRUDE_DRYING_RACK_ITEM.get());
            event.accept(DRYING_RACK_ITEM.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CRUDE_DRYING_RACK_BLOCK_ENTITY.get(),
                (rack, side) -> DryingRackConfig.AUTOMATION_ENABLED.get() ? rack.itemHandler(side) : null
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                DRYING_RACK_BLOCK_ENTITY.get(),
                (rack, side) -> DryingRackConfig.AUTOMATION_ENABLED.get() ? rack.itemHandler(side) : null
        );
    }
}
