package com.protyvkultury.revivalages.feature.technology.campfire;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.campfire.block.CampfireBlock;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.campfire.effect.CampfireEffectEvents;
import com.protyvkultury.revivalages.feature.technology.campfire.effect.CampfireMobEffect;
import com.protyvkultury.revivalages.feature.technology.campfire.item.TinderItem;
import com.protyvkultury.revivalages.feature.technology.campfire.recipe.CampfireRecipe;
import com.protyvkultury.revivalages.feature.technology.campfire.recipe.CampfireRecipeSerializer;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CampfireFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, RevivalAges.MOD_ID);

    public static final DeferredBlock<CampfireBlock> CAMPFIRE = BLOCKS.registerBlock(
            "campfire",
            CampfireBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(0.6F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(CampfireBlock.LIGHT))
    );
    public static final DeferredItem<TinderItem> TINDER = ITEMS.registerItem(
            "tinder",
            TinderItem::new,
            new Item.Properties()
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CampfireBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register(
                    "campfire",
                    () -> BlockEntityType.Builder.of(CampfireBlockEntity::new, CAMPFIRE.get()).build(null)
            );
    public static final DeferredHolder<RecipeType<?>, RecipeType<CampfireRecipe>> RECIPE_TYPE =
            RECIPE_TYPES.register("campfire", simpleRecipeType("campfire"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CampfireRecipe>> RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("campfire", CampfireRecipeSerializer::new);

    public static final DeferredHolder<MobEffect, MobEffect> COMFORT = effect("comfort", 0xD89142);
    public static final DeferredHolder<MobEffect, MobEffect> RESTING = effect("resting", 0x77BFA3);
    public static final DeferredHolder<MobEffect, MobEffect> WELL_FED = effect("well_fed", 0xE6B84A);
    public static final DeferredHolder<MobEffect, MobEffect> WELL_RESTED = effect("well_rested", 0x8CA7E8);
    public static final DeferredHolder<MobEffect, MobEffect> FOCUSED = effect("focused", 0xB47CE8);

    private static DeferredHolder<MobEffect, MobEffect> effect(String id, int color) {
        return EFFECTS.register(id, () -> new CampfireMobEffect(color));
    }

    private static Supplier<RecipeType<CampfireRecipe>> simpleRecipeType(String name) {
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
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        EFFECTS.register(modBus);
        modBus.addListener(this::addCreativeItems);
        modBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.register(CampfireEffectEvents.class);
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(TINDER.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOCK_ENTITY.get(),
                (campfire, side) -> PrimitiveTechnologyConfig.AUTOMATION_ENABLED.get()
                        ? campfire.itemHandler(side)
                        : null
        );
    }
}
