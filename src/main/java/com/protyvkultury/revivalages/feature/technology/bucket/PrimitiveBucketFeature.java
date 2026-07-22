package com.protyvkultury.revivalages.feature.technology.bucket;

import com.mojang.serialization.Codec;
import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.bucket.client.PrimitiveBucketClientEvents;
import com.protyvkultury.revivalages.feature.technology.bucket.item.PrimitiveBucketItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class PrimitiveBucketFeature implements FeatureModule {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RevivalAges.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> BUCKET_FLUID =
            DATA_COMPONENTS.registerComponentType("bucket_fluid", builder -> builder
                    .persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC)
                    .cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BUCKET_USES =
            DATA_COMPONENTS.registerComponentType("bucket_uses", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredItem<PrimitiveBucketItem> WOODEN_BUCKET = ITEMS.registerItem(
            "wooden_bucket", properties -> new PrimitiveBucketItem(PrimitiveBucketItem.Material.WOODEN, properties),
            new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> UNFIRED_CLAY_BUCKET =
            ITEMS.registerSimpleItem("unfired_clay_bucket", new Item.Properties().stacksTo(4));
    public static final DeferredItem<PrimitiveBucketItem> CLAY_BUCKET = ITEMS.registerItem(
            "clay_bucket", properties -> new PrimitiveBucketItem(PrimitiveBucketItem.Material.CLAY, properties),
            new Item.Properties().stacksTo(4));

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        NeoForgeMod.enableMilkFluid();
        DATA_COMPONENTS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.register(PrimitiveBucketEvents.class);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PrimitiveBucketClientEvents.register(modBus);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> ((PrimitiveBucketItem) stack.getItem()).createHandler(stack),
                WOODEN_BUCKET.get(), CLAY_BUCKET.get());
    }
}
