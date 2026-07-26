package com.protyvkultury.revivalages.api.weight;

import net.minecraft.resources.ResourceLocation;

/**
 * Registration surface exposed during the one-time provider registration event.
 */
public interface CarriedWeightRegistrar {

    void registerItemWeightProvider(ResourceLocation id, int priority, ItemWeightProvider provider);

    void registerPlayerWeightSource(ResourceLocation id, int priority, PlayerWeightSource source);

    void registerCapacityProvider(ResourceLocation id, int priority, CapacityProvider provider);

    void registerPocketProvider(ResourceLocation id, int priority, PocketProvider provider);
}
