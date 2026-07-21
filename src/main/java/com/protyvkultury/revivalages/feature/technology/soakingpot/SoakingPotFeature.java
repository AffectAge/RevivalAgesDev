package com.protyvkultury.revivalages.feature.technology.soakingpot;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.soakingpot.block.SoakingPotBlock;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import com.protyvkultury.revivalages.feature.technology.soakingpot.recipe.SoakingPotRecipe;
import com.protyvkultury.revivalages.feature.technology.soakingpot.recipe.SoakingPotRecipeSerializer;
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

public final class SoakingPotFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);

    public static final DeferredBlock<SoakingPotBlock> SOAKING_POT = BLOCKS.registerBlock(
            "soaking_pot",
            SoakingPotBlock::new,
            BlockBehaviour.Properties.of().strength(3.0F, 5.0F).sound(SoundType.STONE).noOcclusion()
    );
    public static final DeferredItem<BlockItem> SOAKING_POT_ITEM = ITEMS.registerSimpleBlockItem(SOAKING_POT, new Item.Properties());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SoakingPotBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "soaking_pot",
            () -> BlockEntityType.Builder.of(SoakingPotBlockEntity::new, SOAKING_POT.get()).build(null)
    );
    public static final DeferredHolder<RecipeType<?>, RecipeType<SoakingPotRecipe>> RECIPE_TYPE = RECIPE_TYPES.register(
            "soaking_pot",
            simpleRecipeType("soaking_pot")
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SoakingPotRecipe>> RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "soaking_pot",
            SoakingPotRecipeSerializer::new
    );

    private static Supplier<RecipeType<SoakingPotRecipe>> simpleRecipeType(String id) {
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
            event.accept(SOAKING_POT_ITEM.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (pot, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get() ? pot.itemHandler(side) : null
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (pot, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get() ? pot.fluidTank() : null
        );
    }
}
