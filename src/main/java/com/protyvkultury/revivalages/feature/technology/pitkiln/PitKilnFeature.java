package com.protyvkultury.revivalages.feature.technology.pitkiln;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnBlock;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitkiln.recipe.PitKilnRecipe;
import com.protyvkultury.revivalages.feature.technology.pitkiln.recipe.PitKilnRecipeSerializer;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PitKilnFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);

    public static final DeferredBlock<PitKilnBlock> PIT_KILN = BLOCKS.registerBlock(
            "pit_kiln",
            PitKilnBlock::new,
            BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GRAVEL).noOcclusion().lightLevel(state ->
                    state.getValue(PitKilnBlock.STAGE) == com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnStage.ACTIVE ? 15 : 0)
    );
    public static final DeferredItem<BlockItem> PIT_KILN_ITEM = ITEMS.registerSimpleBlockItem(PIT_KILN, new Item.Properties());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PitKilnBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "pit_kiln",
            () -> BlockEntityType.Builder.of(PitKilnBlockEntity::new, PIT_KILN.get()).build(null)
    );
    public static final DeferredHolder<RecipeType<?>, RecipeType<PitKilnRecipe>> RECIPE_TYPE = RECIPE_TYPES.register(
            "pit_kiln",
            simpleRecipeType("pit_kiln")
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PitKilnRecipe>> RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "pit_kiln",
            PitKilnRecipeSerializer::new
    );

    private static Supplier<RecipeType<PitKilnRecipe>> simpleRecipeType(String id) {
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
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(PIT_KILN_ITEM.get());
        }
    }
}
