package com.protyvkultury.revivalages.api.weight;

import net.minecraft.server.level.ServerPlayer;

/**
 * Supplies a bounded capacity modifier for one player.
 */
@FunctionalInterface
public interface CapacityProvider {

    CapacityModifier getCapacityModifier(ServerPlayer player);
}
