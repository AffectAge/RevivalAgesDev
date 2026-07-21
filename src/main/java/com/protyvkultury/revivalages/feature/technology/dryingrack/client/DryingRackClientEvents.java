package com.protyvkultury.revivalages.feature.technology.dryingrack.client;

import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class DryingRackClientEvents {

    private DryingRackClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(DryingRackClientEvents::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                DryingRackFeature.CRUDE_DRYING_RACK_BLOCK_ENTITY.get(),
                DryingRackRenderer::new
        );
        event.registerBlockEntityRenderer(
                DryingRackFeature.DRYING_RACK_BLOCK_ENTITY.get(),
                DryingRackRenderer::new
        );
    }
}
