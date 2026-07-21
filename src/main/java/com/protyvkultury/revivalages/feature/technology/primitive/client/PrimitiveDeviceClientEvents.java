package com.protyvkultury.revivalages.feature.technology.primitive.client;

import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class PrimitiveDeviceClientEvents {

    private PrimitiveDeviceClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(PrimitiveDeviceClientEvents::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CampfireFeature.BLOCK_ENTITY.get(), PrimitiveDeviceRenderers.Campfire::new);
        event.registerBlockEntityRenderer(ChoppingBlockFeature.BLOCK_ENTITY.get(), PrimitiveDeviceRenderers.Chopping::new);
        event.registerBlockEntityRenderer(PitKilnFeature.BLOCK_ENTITY.get(), PrimitiveDeviceRenderers.PitKiln::new);
        event.registerBlockEntityRenderer(BarrelFeature.BLOCK_ENTITY.get(), PrimitiveDeviceRenderers.Barrel::new);
        event.registerBlockEntityRenderer(SoakingPotFeature.BLOCK_ENTITY.get(), PrimitiveDeviceRenderers.SoakingPot::new);
        event.registerBlockEntityRenderer(TanningRackFeature.BLOCK_ENTITY.get(), PrimitiveDeviceRenderers.TanningRack::new);
    }
}
