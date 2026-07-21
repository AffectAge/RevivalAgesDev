package com.protyvkultury.revivalages.feature.technology.barrel;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.barrel.item.BarrelBlockItem;
import com.protyvkultury.revivalages.feature.technology.barrel.recipe.BarrelRecipe;
import com.protyvkultury.revivalages.feature.technology.barrel.recipe.BarrelRecipeSerializer;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
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

public final class BarrelFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);

    public static final DeferredBlock<BarrelBlock> BARREL = BLOCKS.registerBlock(
            "barrel",
            BarrelBlock::new,
            BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.WOOD).noOcclusion()
    );
    public static final DeferredItem<BarrelBlockItem> BARREL_ITEM = ITEMS.registerItem(
            "barrel",
            properties -> new BarrelBlockItem(BARREL.get(), properties),
            new Item.Properties()
    );
    public static final DeferredItem<Item> BARREL_LID = ITEMS.registerSimpleItem("barrel_lid", new Item.Properties().stacksTo(16));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BarrelBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "barrel",
            () -> BlockEntityType.Builder.of(BarrelBlockEntity::new, BARREL.get()).build(null)
    );
    public static final DeferredHolder<RecipeType<?>, RecipeType<BarrelRecipe>> RECIPE_TYPE = RECIPE_TYPES.register(
            "barrel",
            simpleRecipeType("barrel")
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BarrelRecipe>> RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "barrel",
            BarrelRecipeSerializer::new
    );

    private static Supplier<RecipeType<BarrelRecipe>> simpleRecipeType(String id) {
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
            event.accept(BARREL_ITEM.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(BARREL_LID.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (barrel, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get()
                        && !barrel.getBlockState().getValue(BarrelBlock.SEALED)
                        && side == Direction.UP
                        ? barrel.itemHandler(side)
                        : null
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (barrel, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get()
                        && !barrel.getBlockState().getValue(BarrelBlock.SEALED)
                        ? barrel.fluidTank()
                        : null
        );
    }
}
