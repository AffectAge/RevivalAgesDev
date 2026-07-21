package com.protyvkultury.revivalages.feature;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

/**
 * A cohesive gameplay feature that owns its registration and lifecycle hooks.
 */
public interface FeatureModule {

    void register(IEventBus modBus, ModContainer modContainer);
}
