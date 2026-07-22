package com.protyvkultury.revivalages.feature.technology.anvil.client;

import com.protyvkultury.revivalages.feature.technology.anvil.AnvilFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class AnvilClientEvents {

    private AnvilClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(AnvilClientEvents::registerRenderer);
    }

    private static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AnvilFeature.BLOCK_ENTITY.get(), AnvilRenderer::new);
    }
}
