package com.protyvkultury.revivalages.feature.technology.primitive;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveFluidClientEvents;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveDeviceClientEvents;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.common.NeoForge;

public final class PrimitiveMaterialsFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, RevivalAges.MOD_ID);
    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, RevivalAges.MOD_ID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RevivalAges.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidStack>> STORED_FLUID =
            DATA_COMPONENTS.registerComponentType("stored_fluid", builder -> builder
                    .persistent(FluidStack.CODEC)
                    .networkSynchronized(FluidStack.STREAM_CODEC)
                    .cacheEncoding());

    public static final DeferredItem<Item> STRAW = item("straw");
    public static final DeferredItem<Item> WOOD_CHIPS = item("wood_chips");
    public static final DeferredItem<Item> PIT_ASH = item("pit_ash");
    public static final DeferredItem<Item> BURNED_FOOD = item("burned_food");
    public static final DeferredItem<Item> UNFIRED_BRICK = item("unfired_brick");
    public static final DeferredItem<Item> RAW_HIDE = item("raw_hide");
    public static final DeferredItem<Item> SCRAPED_HIDE = item("scraped_hide");
    public static final DeferredItem<Item> WASHED_HIDE = item("washed_hide");
    public static final DeferredItem<Item> TANNED_HIDE = item("tanned_hide");

    public static final DeferredBlock<Block> THATCH = BLOCKS.registerSimpleBlock(
            "thatch",
            BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.GRASS).ignitedByLava()
    );
    public static final DeferredItem<net.minecraft.world.item.BlockItem> THATCH_ITEM =
            ITEMS.registerSimpleBlockItem(THATCH, new Item.Properties());

    public static final DeferredHolder<FluidType, FluidType> TANNIN_TYPE = FLUID_TYPES.register(
            "tannin",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.revivalages.tannin")
                    .density(1000)
                    .viscosity(1200)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
            }
    );
    public static final DeferredHolder<Fluid, FlowingFluid> TANNIN = FLUIDS.register(
            "tannin",
            () -> new BaseFlowingFluid.Source(tanninProperties())
    );
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_TANNIN = FLUIDS.register(
            "flowing_tannin",
            () -> new BaseFlowingFluid.Flowing(tanninProperties())
    );
    public static final DeferredBlock<LiquidBlock> TANNIN_BLOCK = BLOCKS.register(
            "tannin",
            () -> new LiquidBlock(TANNIN.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );
    public static final DeferredItem<BucketItem> TANNIN_BUCKET = ITEMS.registerItem(
            "tannin_bucket",
            properties -> new BucketItem(TANNIN.get(), properties),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
    );

    private static DeferredItem<Item> item(String id) {
        return ITEMS.registerSimpleItem(id, new Item.Properties());
    }

    private static BaseFlowingFluid.Properties tanninProperties() {
        return new BaseFlowingFluid.Properties(TANNIN_TYPE, TANNIN, FLOWING_TANNIN)
                .block(TANNIN_BLOCK)
                .bucket(TANNIN_BUCKET)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        DATA_COMPONENTS.register(modBus);
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        NeoForge.EVENT_BUS.register(PrimitiveMaterialEvents.class);
        modBus.addListener(this::addCreativeTabItems);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PrimitiveFluidClientEvents.register(modBus);
            PrimitiveDeviceClientEvents.register(modBus);
        }
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                PrimitiveTechnologyConfig.SPEC,
                "revivalages-primitive-server.toml"
        );
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(STRAW.get());
            event.accept(WOOD_CHIPS.get());
            event.accept(PIT_ASH.get());
            event.accept(BURNED_FOOD.get());
            event.accept(UNFIRED_BRICK.get());
            event.accept(RAW_HIDE.get());
            event.accept(SCRAPED_HIDE.get());
            event.accept(WASHED_HIDE.get());
            event.accept(TANNED_HIDE.get());
        } else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(THATCH_ITEM.get());
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TANNIN_BUCKET.get());
        }
    }
}
