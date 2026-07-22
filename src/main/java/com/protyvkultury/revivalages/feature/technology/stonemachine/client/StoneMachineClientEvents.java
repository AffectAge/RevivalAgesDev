package com.protyvkultury.revivalages.feature.technology.stonemachine.client;

import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class StoneMachineClientEvents {

    private StoneMachineClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(StoneMachineClientEvents::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(StoneMachineFeature.BLOCK_ENTITY.get(), StoneMachineRenderer::new);
    }
}
