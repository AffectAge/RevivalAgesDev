package com.protyvkultury.revivalages.feature.core;

import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.core.registry.CoreBlocks;
import com.protyvkultury.revivalages.feature.core.registry.CoreItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public final class CoreFeature implements FeatureModule {

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        CoreBlocks.register(modBus);
        CoreItems.register(modBus);
    }
}
