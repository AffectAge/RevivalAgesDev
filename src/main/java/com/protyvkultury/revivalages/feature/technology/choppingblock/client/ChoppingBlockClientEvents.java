package com.protyvkultury.revivalages.feature.technology.choppingblock.client;

import com.protyvkultury.revivalages.core.client.render.BlockDamageItemProperties;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.block.ChoppingBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class ChoppingBlockClientEvents {

    private ChoppingBlockClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ChoppingBlockClientEvents::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockDamageItemProperties.register(
                ChoppingBlockFeature.CHOPPING_BLOCK_ITEM.get(), ChoppingBlock.DAMAGE));
    }
}
