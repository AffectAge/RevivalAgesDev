package com.protyvkultury.revivalages.feature.technology.anvil;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.anvil.block.AnvilBlock;
import com.protyvkultury.revivalages.feature.technology.anvil.blockentity.AnvilBlockEntity;
import com.protyvkultury.revivalages.feature.technology.anvil.client.AnvilClientEvents;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilRecipe;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilRecipeSerializer;
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

public final class AnvilFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);

    public static final DeferredBlock<AnvilBlock> ANVIL = BLOCKS.registerBlock(
            "anvil",
            AnvilBlock::new,
            BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(SoundType.STONE).noOcclusion()
    );
    public static final DeferredItem<BlockItem> ANVIL_ITEM = ITEMS.registerSimpleBlockItem(ANVIL, new Item.Properties());
    public static final DeferredItem<Item> STONE_HAMMER = ITEMS.registerSimpleItem(
            "stone_hammer", new Item.Properties().durability(128).stacksTo(1));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnvilBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register("anvil", () -> BlockEntityType.Builder.of(
                    AnvilBlockEntity::new, ANVIL.get()).build(null));
    public static final DeferredHolder<RecipeType<?>, RecipeType<AnvilRecipe>> RECIPE_TYPE =
            RECIPE_TYPES.register("anvil", simpleRecipeType("anvil"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AnvilRecipe>> RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("anvil", AnvilRecipeSerializer::new);

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(this::addCreativeItems);
        modBus.addListener(this::registerCapabilities);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            AnvilClientEvents.register(modBus);
        }
    }

    private static Supplier<RecipeType<AnvilRecipe>> simpleRecipeType(String id) {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id(id).toString();
            }
        };
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ANVIL_ITEM.get());
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(STONE_HAMMER.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (anvil, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get() ? anvil.itemHandler() : null
        );
    }
}
