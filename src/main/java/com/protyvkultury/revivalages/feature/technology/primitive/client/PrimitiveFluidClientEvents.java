package com.protyvkultury.revivalages.feature.technology.primitive.client;

import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class PrimitiveFluidClientEvents {

    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");

    private PrimitiveFluidClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(PrimitiveFluidClientEvents::registerExtensions);
        modBus.addListener(PrimitiveFluidClientEvents::clientSetup);
    }

    private static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return WATER_OVERLAY;
            }

            @Override
            public int getTintColor() {
                return 0xFF6A4A20;
            }
        }, PrimitiveMaterialsFeature.TANNIN_TYPE.get());
    }

    private static void clientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(PrimitiveMaterialsFeature.TANNIN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(PrimitiveMaterialsFeature.FLOWING_TANNIN.get(), RenderType.translucent());
        });
    }
}
