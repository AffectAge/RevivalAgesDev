package com.protyvkultury.revivalages.feature.technology.anvil.client;

import com.protyvkultury.revivalages.core.client.render.BlockDamageItemProperties;
import com.protyvkultury.revivalages.feature.technology.anvil.AnvilFeature;
import com.protyvkultury.revivalages.feature.technology.anvil.block.AnvilBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class AnvilClientEvents {

    private AnvilClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(AnvilClientEvents::registerRenderer);
        modBus.addListener(AnvilClientEvents::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockDamageItemProperties.register(
                AnvilFeature.ANVIL_ITEM.get(), AnvilBlock.DAMAGE));
    }

    private static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AnvilFeature.BLOCK_ENTITY.get(), AnvilRenderer::new);
    }
}
