package com.protyvkultury.revivalages.feature.technology.animalpower;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.animalpower.block.AnimalMachineBlock;
import com.protyvkultury.revivalages.feature.technology.animalpower.block.HandGrindstoneBlock;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.AnimalMachineBlockEntity;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.HandGrindstoneBlockEntity;
import com.protyvkultury.revivalages.feature.technology.animalpower.client.AnimalPowerClientEvents;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipe;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipeSerializer;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.PressingRecipe;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.PressingRecipeSerializer;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.WoodVariantChoppingBlockRecipe;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AnimalPowerFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RevivalAges.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> WOOD_VARIANT =
            DATA_COMPONENTS.registerComponentType("wood_variant", builder -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .cacheEncoding());

    public static final DeferredBlock<HandGrindstoneBlock> HAND_GRINDSTONE = BLOCKS.registerBlock(
            "hand_grindstone",
            HandGrindstoneBlock::new,
            machineProperties(SoundType.STONE)
    );
    public static final DeferredBlock<AnimalMachineBlock> HORSE_GRINDSTONE = BLOCKS.registerBlock(
            "horse_grindstone",
            properties -> new AnimalMachineBlock(AnimalMachineKind.GRINDSTONE, properties),
            machineProperties(SoundType.STONE)
    );
    public static final DeferredBlock<AnimalMachineBlock> HORSE_CHOPPING_BLOCK = BLOCKS.registerBlock(
            "horse_chopping_block",
            properties -> new AnimalMachineBlock(AnimalMachineKind.CHOPPING_BLOCK, properties),
            machineProperties(SoundType.WOOD)
    );
    public static final DeferredBlock<AnimalMachineBlock> HORSE_PRESS = BLOCKS.registerBlock(
            "horse_press",
            properties -> new AnimalMachineBlock(AnimalMachineKind.PRESS, properties),
            machineProperties(SoundType.WOOD)
    );

    public static final DeferredItem<BlockItem> HAND_GRINDSTONE_ITEM =
            ITEMS.registerSimpleBlockItem(HAND_GRINDSTONE, new Item.Properties());
    public static final DeferredItem<BlockItem> HORSE_GRINDSTONE_ITEM =
            ITEMS.registerSimpleBlockItem(HORSE_GRINDSTONE, new Item.Properties());
    public static final DeferredItem<BlockItem> HORSE_CHOPPING_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(HORSE_CHOPPING_BLOCK, new Item.Properties());
    public static final DeferredItem<BlockItem> HORSE_PRESS_ITEM =
            ITEMS.registerSimpleBlockItem(HORSE_PRESS, new Item.Properties());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HandGrindstoneBlockEntity>>
            HAND_GRINDSTONE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
                    "hand_grindstone",
                    () -> BlockEntityType.Builder.of(HandGrindstoneBlockEntity::new, HAND_GRINDSTONE.get())
                            .build(null)
            );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnimalMachineBlockEntity>>
            ANIMAL_MACHINE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
                    "animal_machine",
                    () -> BlockEntityType.Builder.of(
                            AnimalMachineBlockEntity::new,
                            HORSE_GRINDSTONE.get(),
                            HORSE_CHOPPING_BLOCK.get(),
                            HORSE_PRESS.get()
                    ).build(null)
            );

    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING_TYPE =
            RECIPE_TYPES.register("grinding", simpleRecipeType("grinding"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", GrindingRecipeSerializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<PressingRecipe>> PRESSING_TYPE =
            RECIPE_TYPES.register("pressing", simpleRecipeType("pressing"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PressingRecipe>> PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("pressing", PressingRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<WoodVariantChoppingBlockRecipe>>
            WOOD_VARIANT_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "wood_variant_chopping_block",
                    () -> new SimpleCraftingRecipeSerializer<>(WoodVariantChoppingBlockRecipe::new)
            );

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        DATA_COMPONENTS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(this::addCreativeItems);
        modBus.addListener(this::registerCapabilities);
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                AnimalPowerConfig.SPEC,
                "revivalages-animal-power-server.toml"
        );
        if (FMLEnvironment.dist == Dist.CLIENT) {
            AnimalPowerClientEvents.register(modBus);
        }
    }

    public static AnimalMachineKind kind(net.minecraft.world.level.block.state.BlockState state) {
        return state.getBlock() instanceof AnimalMachineBlock machine
                ? machine.kind()
                : AnimalMachineKind.GRINDSTONE;
    }

    private static BlockBehaviour.Properties machineProperties(SoundType sound) {
        return BlockBehaviour.Properties.of()
                .strength(2.5F)
                .sound(sound)
                .noOcclusion();
    }

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> Supplier<RecipeType<T>> simpleRecipeType(
            String id
    ) {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id(id).toString();
            }
        };
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(HAND_GRINDSTONE_ITEM.get());
            event.accept(HORSE_GRINDSTONE_ITEM.get());
            event.accept(HORSE_CHOPPING_BLOCK_ITEM.get());
            event.accept(HORSE_PRESS_ITEM.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ANIMAL_MACHINE_BLOCK_ENTITY.get(),
                (machine, side) -> AnimalPowerConfig.AUTOMATION_ENABLED.get()
                        ? machine.itemHandler(side)
                        : null
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ANIMAL_MACHINE_BLOCK_ENTITY.get(),
                (machine, side) -> AnimalPowerConfig.AUTOMATION_ENABLED.get()
                        && machine.kind() == AnimalMachineKind.PRESS
                        && side == net.minecraft.core.Direction.DOWN
                        ? machine.fluidOutputHandler()
                        : null
        );
    }
}
