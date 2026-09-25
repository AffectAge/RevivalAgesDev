package com.protyvkultury.revivalages.feature.technology.animalpower;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import com.protyvkultury.revivalages.feature.technology.animalpower.block.HandGrindstoneBlock;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.HandGrindstoneBlockEntity;
import com.protyvkultury.revivalages.feature.technology.animalpower.client.AnimalPowerClientEvents;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipe;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipeSerializer;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
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
    public static final DeferredBlock<HandGrindstoneBlock> HAND_GRINDSTONE = BLOCKS.registerBlock(
            "hand_grindstone",
            HandGrindstoneBlock::new,
            machineProperties(SoundType.STONE)
    );

    public static final DeferredItem<BlockItem> HAND_GRINDSTONE_ITEM =
            ITEMS.registerSimpleBlockItem(HAND_GRINDSTONE, new Item.Properties());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HandGrindstoneBlockEntity>>
            HAND_GRINDSTONE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
                    "hand_grindstone",
                    () -> BlockEntityType.Builder.of(HandGrindstoneBlockEntity::new, HAND_GRINDSTONE.get())
                            .build(null)
            );
    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING_TYPE =
            RECIPE_TYPES.register("grinding", simpleRecipeType("grinding"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", GrindingRecipeSerializer::new);

    @Override
    public ContentPolicy contentPolicy() {
        return ContentPolicy.gameplay("hand_grinding")
                .define(ContentKey.HAND_GRINDSTONE)
                .items(ContentKey.HAND_GRINDSTONE, "hand_grindstone")
                .build();
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            AnimalPowerClientEvents.register(modBus);
        }
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

}
