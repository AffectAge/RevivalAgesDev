package com.protyvkultury.revivalages.feature.technology.choppingblock;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.choppingblock.block.ChoppingBlock;
import com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity.ChoppingBlockEntity;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipeSerializer;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChoppingBlockFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);

    public static final DeferredBlock<ChoppingBlock> CHOPPING_BLOCK = BLOCKS.registerBlock(
            "chopping_block",
            ChoppingBlock::new,
            BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.WOOD).noOcclusion()
    );
    public static final DeferredItem<BlockItem> CHOPPING_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(CHOPPING_BLOCK, new Item.Properties());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChoppingBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "chopping_block",
            () -> BlockEntityType.Builder.of(ChoppingBlockEntity::new, CHOPPING_BLOCK.get()).build(null)
    );
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChoppingRecipe>> RECIPE_TYPE = RECIPE_TYPES.register(
            "chopping",
            simpleRecipeType("chopping")
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ChoppingRecipe>> RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "chopping",
            ChoppingRecipeSerializer::new
    );

    private static Supplier<RecipeType<ChoppingRecipe>> simpleRecipeType(String id) {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id(id).toString();
            }
        };
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(this::addCreativeItems);
        modBus.addListener(this::registerCapabilities);
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CHOPPING_BLOCK_ITEM.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (block, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get() ? block.itemHandler(side) : null
        );
    }
}
