package com.protyvkultury.revivalages.feature;

import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

/**
 * A cohesive gameplay feature that owns its registration and lifecycle hooks.
 */
public interface FeatureModule {

    /**
     * Declares whether this module is infrastructure or which gameplay content it owns.
     */
    ContentPolicy contentPolicy();

    void register(IEventBus modBus, ModContainer modContainer);
}
