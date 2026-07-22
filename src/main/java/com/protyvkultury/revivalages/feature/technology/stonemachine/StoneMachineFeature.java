package com.protyvkultury.revivalages.feature.technology.stonemachine;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneCrucibleBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneKilnBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneMachineBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneOvenBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneSawmillBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.blockentity.StoneMachineBlockEntity;
import com.protyvkultury.revivalages.feature.technology.stonemachine.client.StoneMachineClientEvents;
import com.protyvkultury.revivalages.feature.technology.stonemachine.recipe.StoneCrucibleRecipe;
import com.protyvkultury.revivalages.feature.technology.stonemachine.recipe.StoneCrucibleRecipeSerializer;
import com.protyvkultury.revivalages.feature.technology.stonemachine.recipe.StoneKilnRecipe;
import com.protyvkultury.revivalages.feature.technology.stonemachine.recipe.StoneKilnRecipeSerializer;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class StoneMachineFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, RevivalAges.MOD_ID);

    public static final DeferredBlock<StoneSawmillBlock> STONE_SAWMILL = BLOCKS.registerBlock(
            "stone_sawmill", StoneSawmillBlock::new, machineProperties());
    public static final DeferredBlock<StoneOvenBlock> STONE_OVEN = BLOCKS.registerBlock(
            "stone_oven", StoneOvenBlock::new, machineProperties());
    public static final DeferredBlock<StoneKilnBlock> STONE_KILN = BLOCKS.registerBlock(
            "stone_kiln", StoneKilnBlock::new, machineProperties());
    public static final DeferredBlock<StoneCrucibleBlock> STONE_CRUCIBLE = BLOCKS.registerBlock(
            "stone_crucible", StoneCrucibleBlock::new, machineProperties());

    public static final DeferredItem<BlockItem> STONE_SAWMILL_ITEM =
            ITEMS.registerSimpleBlockItem(STONE_SAWMILL, new Item.Properties());
    public static final DeferredItem<BlockItem> STONE_OVEN_ITEM =
            ITEMS.registerSimpleBlockItem(STONE_OVEN, new Item.Properties());
    public static final DeferredItem<BlockItem> STONE_KILN_ITEM =
            ITEMS.registerSimpleBlockItem(STONE_KILN, new Item.Properties());
    public static final DeferredItem<BlockItem> STONE_CRUCIBLE_ITEM =
            ITEMS.registerSimpleBlockItem(STONE_CRUCIBLE, new Item.Properties());

    public static final DeferredItem<Item> STONE_SAW_BLADE = ITEMS.registerSimpleItem(
            "stone_saw_blade", new Item.Properties().durability(64));
    public static final DeferredItem<Item> FLINT_SAW_BLADE = ITEMS.registerSimpleItem(
            "flint_saw_blade", new Item.Properties().durability(96));
    public static final DeferredItem<Item> BONE_SAW_BLADE = ITEMS.registerSimpleItem(
            "bone_saw_blade", new Item.Properties().durability(96));

    public static final DeferredHolder<SoundEvent, SoundEvent> SAWMILL_IDLE = sound("sawmill_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SAWMILL_ACTIVE = sound("sawmill_active");
    public static final DeferredHolder<SoundEvent, SoundEvent> SAWMILL_ACTIVE_SHORT_A =
            sound("sawmill_active_short_a");
    public static final DeferredHolder<SoundEvent, SoundEvent> SAWMILL_ACTIVE_SHORT_B =
            sound("sawmill_active_short_b");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoneMachineBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register("stone_machine", () -> BlockEntityType.Builder.of(
                    StoneMachineBlockEntity::new,
                    STONE_SAWMILL.get(),
                    STONE_OVEN.get(),
                    STONE_KILN.get(),
                    STONE_CRUCIBLE.get()
            ).build(null));

    public static final DeferredHolder<RecipeType<?>, RecipeType<StoneKilnRecipe>> STONE_KILN_RECIPE_TYPE =
            RECIPE_TYPES.register("stone_kiln", simpleRecipeType("stone_kiln"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StoneKilnRecipe>>
            STONE_KILN_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "stone_kiln", StoneKilnRecipeSerializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<StoneCrucibleRecipe>> STONE_CRUCIBLE_RECIPE_TYPE =
            RECIPE_TYPES.register("stone_crucible", simpleRecipeType("stone_crucible"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StoneCrucibleRecipe>>
            STONE_CRUCIBLE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "stone_crucible", StoneCrucibleRecipeSerializer::new);

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        SOUND_EVENTS.register(modBus);
        modBus.addListener(this::addCreativeItems);
        modBus.addListener(this::registerCapabilities);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            StoneMachineClientEvents.register(modBus);
        }
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .strength(2.0F, 15.0F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .lightLevel(state -> state.getValue(StoneMachineBlock.LIT) ? 13 : 0);
    }

    private static <T extends Recipe<?>> Supplier<RecipeType<T>> simpleRecipeType(String id) {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id(id).toString();
            }
        };
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String id) {
        return SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(RevivalAges.id(id)));
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(STONE_SAWMILL_ITEM.get());
            event.accept(STONE_OVEN_ITEM.get());
            event.accept(STONE_KILN_ITEM.get());
            event.accept(STONE_CRUCIBLE_ITEM.get());
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(STONE_SAW_BLADE.get());
            event.accept(FLINT_SAW_BLADE.get());
            event.accept(BONE_SAW_BLADE.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (machine, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get()
                        ? machine.itemHandler(side)
                        : null
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (machine, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get()
                        ? machine.fluidHandler(side)
                        : null
        );
    }
}
